package online.twelvesteps.anarucombot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.twelvesteps.anarucombot.CommandExecutionContext.UpdateKind;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.adminrights.SetMyDefaultAdministratorRights;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.adminrights.ChatAdministratorRights;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;
import static online.twelvesteps.anarucombot.Stringers.stringify;
import static online.twelvesteps.anarucombot.Stringers.strippedNotEmpty;

@Slf4j
final class Bot extends TelegramLongPollingBot {
  private static final String DEFAULT_BOTNAME = "anarucombot";
  private static final String ENV_NAME = "ANARUCOMBOTTOKEN";

  public static void main(String... args) throws Exception {
    EasyCommandReactionBuilder<EasyExecutionContext> commands = buildReactions();
    Bot theBot = new Bot(getBotname(args), getBottoken(), commands.reactions());
    System.out.println("Started on behalf of " + theBot.getBotUsername());
    new TelegramBotsApi(DefaultBotSession.class).registerBot(theBot);

    {
      User botuser = theBot.getMe();
      log.info("The bot is authorized: " + stringify(botuser));
      if (!theBot.getBotUsername().equals(botuser.getUserName())) {
        System.err.printf("""
                Wrong bot!
                Started on behalf of %s,
                but authorized as %s.
                Terminated
                """,
            theBot.getBotUsername(), botuser.getUserName());
        usage();
        System.exit(126);
      }
    }

    {
      // set my admin privileges
      val rights = new ChatAdministratorRights();
      rights.setCanDeleteMessages(true);
      rights.setCanManageVideoChats(true);
      rights.setCanRestrictMembers(true);
      rights.setCanPromoteMembers(true);
      rights.setCanInviteUsers(true);
      theBot.execute(new SetMyDefaultAdministratorRights(rights, null));
    }

    {
      log.info("Clearing all commands previously might be set. May be forbidden for chats this bot is not a member of now");
      for (val i : SERVED_CHATS.keySet()) {
        log.info("Clearing all commands for chat [{}]", SERVED_CHATS.get(i));
        logFailure(() -> theBot.execute(new DeleteMyCommands(new BotCommandScopeChatAdministrators(String.valueOf(i)), null)));
      }
      logFailure(() -> theBot.execute(new DeleteMyCommands(new BotCommandScopeAllChatAdministrators(), null)));
      logFailure(() -> theBot.execute(new DeleteMyCommands(new BotCommandScopeDefault(), null)));
    }

    // tell users supported commands
    for (val i : SERVED_CHATS.keySet()) {
      log.info("Setting commands for chat [{}]", SERVED_CHATS.get(i));
      logFailure(() -> theBot.execute(new SetMyCommands(
          commands.commandsWithDescription(),
          new BotCommandScopeChatAdministrators(String.valueOf(i)),
          null)));
    }
  }

  private static EasyCommandReactionBuilder<EasyExecutionContext> buildReactions() {
    return new EasyCommandReactionBuilder<>() {
      {
        // @formatter:off
        bind("1", "Что такое АНА?"          , replaceWith("1_what_is"      ));
        bind("2", "Утверждение цели АНА"    , replaceWith("2_the_goal"     ));
        bind("3", "12 шагов АНА"            , replaceWith("3_steps"        ));
        bind("4", "12 традиций АНА"         , replaceWith("4_traditions"   ));
        bind("5", "Выход есть"              , replaceWith("5_solution"     ));
        bind("6", "Приглашаем бога"         , replaceWith("6_prey_inviting" ));
        bind("7", "Молитва о душевном покое", replaceWith("7_prey_serenity"));
        bind("8", "Ссылки"                  , replaceWith(chain(
                                                 resource("8_links"        ),
                                                 resource("9_ads"          ))));
        // ----------------------------------------------------------------
        bind("help" , "Покажу, что могу (но позже)", resource("help" ).ifReacted(clearTimestamp()));
        bind("start", "Включи меня"                , resource("start").ifReacted(clearTimestamp()));
        // @formatter:on
      }
    };
  }

  private static String getBotname(String... args) {
    return args.length >= 1 ? args[0] : DEFAULT_BOTNAME;
  }

  private static void usage() {
    // to avoid reordering with printing to STDERR
    try { Thread.sleep(10); }
    catch(InterruptedException ignore) {}
    System.out.printf("""
            Usage: %s="$BOTTOKEN" java -jar anarucombot.jar [BOTNAME]
            BOTNAME - %s (default)
                      anarucomalfabot (mind the 'alfa')
            """,
        ENV_NAME,
        DEFAULT_BOTNAME);
  }

  private static String getBottoken() {
    String bottoken = System.getenv(ENV_NAME);
    String errLine
        = bottoken == null ? "No " + ENV_NAME + " environment variable value found"
        : bottoken.isEmpty() ? ENV_NAME + " environment variable is blank"
        : bottoken.length() != bottoken.strip().length() ? ENV_NAME + " environment variable is not stripped"
        : null;
    if (errLine != null) {
      System.err.println(errLine);
      usage();
      System.exit(127);
    }
    return bottoken;
  }

  private static final Map<Long, String> SERVING_BOTS = Map.of(
      1881706076L, "anarucombot",
      5842456956L, "anarucomalfabot");
  private static final Map<Long, String> SERVED_CHATS = Map.of(
      -1001636629132L, "Прожарка бота",
      -1001640782633L, "АНА Онлайн");
  private final EasyExecutionContext executionContext = new EasyExecutionContext(this);
  private final Map<String, BotReaction<?, EasyExecutionContext, ?>> reactions;
  private final String bottoken;
  private final String botname;

  private Bot(String botname, String bottoken,
      Map<String, BotReaction<?, EasyExecutionContext, ?>> reactions) {
    super(botOptions());
    this.botname  = strippedNotEmpty(botname , "botname" );
    this.bottoken = strippedNotEmpty(bottoken, "bottoken");
    this.reactions = checkNotNull(reactions, "reactions = null");
  }

  private static DefaultBotOptions botOptions() {
    val options = new DefaultBotOptions();
    options.setAllowedUpdates(Arrays.asList(
        "message",
        "edited_message",
        "channel_post",
        "edited_channel_post",
        "callback_query",
        "chat_member",
        "chat_join_request"));
    return options;
  }

  @Override
  public String getBotUsername() {
    return botname;
  }

  @Override
  public String getBotToken() {
    return bottoken;
  }

  @Override
  public void onUpdateReceived(Update update) {
    UpdateKind updateKind = executionContext.update(update);
    if (updateKind == UpdateKind.UPDATE) {
      if (update.hasEditedMessage()) {
        updateKind = UpdateKind.MESSAGE;
        if (update.getEditedMessage().hasText()) {
          updateKind = UpdateKind.TEXT;
        }
      }
    }
    switch (updateKind) {
      case COMMAND -> {
        final Message msg = update.getMessage();
        final Chat chat = msg.getChat();
        final User from = msg.getFrom();
        final Map<Long, String> adminSet = getCachedAdminSetFor(chat.getId());
        BotReaction<?, EasyExecutionContext, ?> reaction;
        String warnMsg = null;
        Throwable err = null;
        if (executionContext.getReceivedTargetBotname() != null
        && !executionContext.getReceivedTargetBotname().equals(getBotUsername())) {
          warnMsg = "onUpdateReceived: in {} by {}: unexpected bot: {}";
        } else if (!SERVED_CHATS.containsKey(chat.getId())) {
          warnMsg = "onUpdateReceived: in {} by {}: command from unsupported chat: {}";
        } else if (!adminSet.containsKey(from.getId())) {
          warnMsg = "onUpdateReceived: in {} by {}: non-admin sent: {}";
        } else if ((reaction = reactions.get(executionContext.getReceivedCommand())) == null) {
          warnMsg = "onUpdateReceived: in {} by {}: no reaction for: {}";
        } else {
          try {
            reaction.react(executionContext);
          } catch (TelegramApiException ex) {
            warnMsg = "onUpdateReceived: in {} by {}: failed to react to: {}";
            err = ex;
          }
        }
        if (warnMsg != null) {
          warnMsg += "; deleting the message";
          log.warn(warnMsg, stringify(chat), stringify(from), msg.getText(), err);
          logFailure(() -> new DeleteMessageReaction<>().react(executionContext));
        }
      }
      case TEXT    -> { /* swallow */ }
      case MESSAGE -> {
        final Message msg = update.getMessage();
        if (!isEmpty(msg.getNewChatMembers()) || msg.getLeftChatMember() != null) {
          // NOTE: this is just a message that appears in a chat
          // NOTE: actual update is communicated via Update object
          // check if the message is about anarucombots ONLY delete it
          final User mold = msg.getLeftChatMember();
          final List<User> mnew = msg.getNewChatMembers();
          val uniqueMemberIds = new HashSet<Long>();
          if (!isEmpty(mnew)) {
            uniqueMemberIds.addAll(mnew.stream().map(User::getId).toList());
          }
          if (mold != null) {
            uniqueMemberIds.add(mold.getId());
          }
          uniqueMemberIds.removeAll(SERVING_BOTS.keySet());
          if (uniqueMemberIds.isEmpty()) {
            logFailure(() -> new DeleteMessageReaction<>().react(executionContext));
          }
        } else if (msg.getDocument() == null // gif?
                && msg.getPhoto   () == null
                && msg.getVideo   () == null
                && msg.getVideoChatScheduled() == null
                && msg.getVideoChatStarted  () == null
                && msg.getVideoChatEnded    () == null) {
          log.info("onUpdateReceived: in {} by {}: non–text msg",
              stringify(msg.getChat()),
              stringify(msg.getFrom()));
          serialize(update, "non-text-msg.ser");
        }
      }
      case UPDATE -> {
        if (update.hasMyChatMember()) {
          // this bot was either added to or removed from a chat
          ChatMemberUpdated msg = update.getMyChatMember();
          log.info("onUpdateReceived: in {} by {}: this bot: {} ——> {}",
              stringify(msg.getChat()),
              stringify(msg.getFrom()),
              stringify(msg.getOldChatMember()),
              stringify(msg.getNewChatMember()));
        } else if (update.hasChatMember()) {
          // changes in chat members
          final ChatMemberUpdated msg = update.getChatMember();
          final Chat chat = msg.getChat();
          final ChatMember mold = msg.getOldChatMember();
          final ChatMember mnew = msg.getNewChatMember();
          log.info("onUpdateReceived: in {} by {}: member: {} ——> {}",
              stringify(chat),
              stringify(msg.getFrom()),
              stringify(mold),
              stringify(mnew));
          val admin = CHAT_MEMBER_STATUS_ADMIN; // just for brevity
          val statuses = Set.of(mold.getStatus(), mnew.getStatus());
          if (Objects.equals(mold.getUser().getId(), mnew.getUser().getId())
          && !Objects.equals(mold.getStatus(), mnew.getStatus())
          && statuses.contains(admin)) { // is any admin?
            val chatId = chat.getId();
            val adminId = mold.getUser().getId();
            Map<Long, String> adminSet = getCachedAdminSetFor(chatId);
            if (admin.equals(mold.getStatus())) {
              adminSet.remove(adminId); // was admin
            } else {
              adminSet.put(adminId, stringify(mold.getUser())); // became admin
            }
          }
        } else if (update.hasCallbackQuery()) {
          CallbackQuery query = update.getCallbackQuery();
          // TODO: fix
          log.info("onUpdateReceived: in {} by {}: callback query",
              stringify(query),
              stringify(query.getFrom()));
          serialize(update, "callback-query.ser");
        } else {
          log.info("onUpdateReceived: raw: {}", update);
          serialize(update, "service-update.ser");
        }
      }
      default -> throw new BadDeploymentError("No reaction for " + updateKind);
    }
  }

  private static final String CHAT_MEMBER_STATUS_ADMIN = "administrator";

  // map is just for debugging purposes
  private final LoadingCache<Long, Map<Long, String>> chatAdminSetCache
      = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @SuppressWarnings("NullableProblems") @Override
        public Map<Long, String> load(Long chatId) throws TelegramApiException {
          return execute(new GetChatAdministrators(String.valueOf(chatId)))
              .stream()
              .map(ChatMember::getUser)
              .collect(toMap(User::getId, Stringers::stringify));
        }
      });

  private Map<Long, String> getCachedAdminSetFor(Long chatId) {
    try {
      return chatAdminSetCache.getUnchecked(chatId);
    } catch (UncheckedExecutionException ex) {
      log.warn("getCachedAdminSetFor:"
          + " Failed to load chat admin set for {}."
          + " Postponed to the next retrieval",
          chatId, ex.getCause());
      return new HashMap<>(1, 1F);
    }
  }

  private static boolean isEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }

  private static void logFailure(Callable<?> call) {
    try {
      call.call();
    } catch (Throwable err) {
      log.warn("http call failed", err);
    }
  }

  private static void serialize(Serializable what, String to) {
    try (ObjectOutputStream os = new ObjectOutputStream(
        new BufferedOutputStream(
            Files.newOutputStream(
                Paths.get(to))))) {
      os.writeObject(what);
      log.info("Serialized to {}", to);
    } catch (IOException ex) {
      log.error("Failed to serialize to {}", to, ex);
    }
  }
}
