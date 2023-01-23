package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.twelvesteps.anarucombot.CommandExecutionContext.UpdateKind;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.adminrights.SetMyDefaultAdministratorRights;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.adminrights.ChatAdministratorRights;
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
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
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
    this.botname  = strippedNotEmpty(botname , "botname" );
    this.bottoken = strippedNotEmpty(bottoken, "bottoken");
    this.reactions = checkNotNull(reactions, "reactions = null");
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
    switch (updateKind) {
      case COMMAND -> {
        final Message msg = update.getMessage();
        final Chat chat = msg.getChat();
        BotReaction<?, EasyExecutionContext, ?> reaction
            = reactions.get(executionContext.getReceivedCommand());
        String warnMsg = null;
        if (executionContext.getReceivedTargetBotname() != null
            && !executionContext.getReceivedTargetBotname().equals(getBotUsername())) {
          warnMsg = "onUpdateReceived: unexpected bot: [{}] sent to {} by {}; trying to delete the message";
        } else if (!SERVED_CHATS.containsKey(chat.getId())) {
          warnMsg = "onUpdateReceived: command from unsupported chat: [{}] sent to {} by {}";
        } else if (reaction == null) {
          warnMsg = "onUpdateReceived: no reaction for [{}] sent to {} by {}";
        }
        if (warnMsg == null) {
          try {
            reaction.react(executionContext);
          } catch (TelegramApiException ex) {
            log.warn("onUpdateReceived: failed to react to [{}] sent to {} by {}",
                msg.getText(),
                stringify(chat),
                stringify(msg.getFrom()),
                ex);
          }
        } else {
          log.warn(warnMsg, msg.getText(), stringify(chat), stringify(msg.getFrom()));
          logFailure(() -> new DeleteMessageReaction<>().react(executionContext));
        }
      }
      case TEXT    -> { /* swallow */ }
      case MESSAGE -> {
        final Message msg = update.getMessage();
        if (msg.getNewChatMembers() != null || msg.getLeftChatMember() != null) {
          log.info("onUpdateReceived: new or left chat members of {} by {}:"
              + "\n\tnew chat members: {}"
              + "\n\tleft chat member: {}",
              stringify(msg.getChat()),
              stringify(msg.getFrom()),
              msg.getNewChatMembers() == null ? null
                  : msg.getNewChatMembers().stream().map(Stringers::stringify).toList(),
              stringify(msg.getLeftChatMember()));
          // check if the message is about anarucombots ONLY delete it
          val uniqueMemberIds = new HashSet<Long>();
          if (msg.getNewChatMembers() != null) {
            uniqueMemberIds.addAll(
                msg.getNewChatMembers().stream().map(User::getId).toList());
          }
          if (msg.getLeftChatMember() != null) {
            uniqueMemberIds.add(msg.getLeftChatMember().getId());
          }
          uniqueMemberIds.removeAll(SERVING_BOTS.keySet());
          if (uniqueMemberIds.isEmpty()) {
            logFailure(() -> new DeleteMessageReaction<>().react(executionContext));
          }
        } else {
          log.info("onUpdateReceived: non–text msg sent to {} by {}",
              stringify(msg.getChat()),
              stringify(msg.getFrom()));
          serialize(update, "non-text-msg.ser");
        }
      }
      default -> {
        if (!update.hasCallbackQuery()) {
          log.info("onUpdateReceived: no message in update: {}", update);
          serialize(update, "service-update.ser");
        } else {
          CallbackQuery query = update.getCallbackQuery();
          log.info("onUpdateReceived: callback query: {} sent by {}",
              stringify(query),
              stringify(query.getFrom()));
          serialize(update, "callback-query.ser");
        }
      }
    }
  }

  private static void logFailure(Callable<?> call) {
    try {
      call.call();
    } catch (Throwable err) {
      log.warn("http call failed", err);
    }
  }

  private void serialize(Serializable what, String to) {
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
