package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.twelvesteps.anarucombot.CommandExecutionContext.UpdateKind;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.adminrights.SetMyDefaultAdministratorRights;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.adminrights.ChatAdministratorRights;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllChatAdministrators;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
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

    User user = theBot.getMe();
    log.info("The bot is authorized: " + stringify(user));
    if (!theBot.getBotUsername().equals(user.getUserName())) {
      System.err.printf("""
          Wrong bot!
          Started on behalf of %s,
          but authorized as %s.
          Terminated
          """,
          theBot.getBotUsername(), user.getUserName());
      usage();
      System.exit(126);
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

    // tell users supported commands
    theBot.execute(new SetMyCommands(
        commands.commandsWithDescription(),
        new BotCommandScopeAllChatAdministrators(),
        null));
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
        bind("6", "Приглашаем бога"         , replaceWith("6_prey_opening" ));
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
        if (executionContext.getReceivedTargetBotname() != null
            && !executionContext.getReceivedTargetBotname().equals(getBotUsername())) {
          log.warn("onUpdateReceived: unexpected bot: [{}] sent to {} by {}",
              msg.getText(),
              stringify(chat),
              stringify(msg.getFrom()));
        } else if (!SERVED_CHATS.containsKey(chat.getId())) {
          log.warn("onUpdateReceived: command from unsupported chat: [{}] sent to {} by {}",
              msg.getText(),
              stringify(chat),
              stringify(msg.getFrom()));
        } else if (reaction == null) {
          log.warn("onUpdateReceived: no reaction for command: [{}] sent to {} by {}",
              msg.getText(),
              stringify(chat),
              stringify(msg.getFrom()));
        } else {
          try {
            reaction.react(executionContext);
          } catch (TelegramApiException ex) {
            log.error(format("onUpdateReceived: [%s] sent to %s by %s",
                msg.getText(),
                stringify(chat),
                stringify(msg.getFrom())),
                ex);
          }
        }
      }
      case TEXT    -> { /* swallow */ }
      case MESSAGE -> {
        final Message msg = update.getMessage();
        if (msg.getNewChatMembers() != null || msg.getLeftChatMember() != null) {
          log.info("onUpdateReceived: non–text msg sent to {} by {}:"
              + "\n\tnew chat members: {}"
              + "\n\tleft chat member: {}",
              stringify(msg.getChat()),
              stringify(msg.getFrom()),
              msg.getNewChatMembers().stream().map(Stringers::stringify).collect(toList()),
              stringify(msg.getLeftChatMember()));
          try {
            new DeleteMessageReaction<>().react(executionContext);
          } catch (TelegramApiException ex) {
            log.error("onUpdateReceived: [{}] sent to {} by {}",
                msg.getText(),
                stringify(msg.getChat()),
                stringify(msg.getFrom()),
                ex);
          }
        } else {
          log.info("onUpdateReceived: non–text msg sent to {} by {}",
              stringify(msg.getChat()),
              stringify(msg.getFrom()));
          final String file = "user-added-another-msg.ser";
          try (val os = new ObjectOutputStream(
              new BufferedOutputStream(
                  Files.newOutputStream(
                      Paths.get(file))))) {
            os.writeObject(update);
            log.info("onUpdateReceived: serialized to {}", file);
          } catch (IOException ex) {
            log.error("onUpdateReceived: on serializing to {} received non-msg update", file, ex);
          }
        }
      }
      default -> {
        if (!update.hasCallbackQuery()) {
          log.info("onUpdateReceived: no message in update: {}", update);
        } else {
          CallbackQuery query = update.getCallbackQuery();
          log.info("onUpdateReceived: callback query: {} sent by {}",
              stringify(query),
              stringify(query.getFrom()));
        }
      }
    }
  }
}
