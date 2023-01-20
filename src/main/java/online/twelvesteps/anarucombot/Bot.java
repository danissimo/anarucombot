package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllChatAdministrators;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static online.twelvesteps.anarucombot.Stringers.strippedNotEmpty;

@Slf4j
final class Bot extends TelegramLongPollingBot {
  private static final String ENV_TOKEN = "ANARUCOMBOTTOKEN";

  public static void main(String... args) throws Exception {
    String bottoken = System.getenv(ENV_TOKEN);
    {
      String errLine
          = bottoken == null ? "No " + ENV_TOKEN + " environment variable value found"
          : bottoken.isEmpty() ? ENV_TOKEN + " environment variable is blank"
          : bottoken.length() != bottoken.strip().length() ? ENV_TOKEN + " environment variable is not stripped"
          : null;
      if (errLine != null) {
        System.err.println(errLine
            + "\nUsage: " + ENV_TOKEN + "=\"$TOKEN\" java -jar anarucombot.jar");
        System.exit(127);
      }
    }
    Bot theBot = new Bot(bottoken);
    System.out.println("Started on behalf of " + theBot.botname);
    new TelegramBotsApi(DefaultBotSession.class).registerBot(theBot);
    User user = theBot.getMe();
    log.info("The bot is authorized: " + Stringers.toString(user));
    if (!theBot.botname.equals(user.getUserName())) {
      System.out.printf("""
          Wrong bot!
          Started on behalf of %s,
          but authorized as %s.
          Terminated
          """,
          theBot.botname, user.getUserName());
      System.exit(126);
    }
    theBot.execute(new SetMyCommands(
        new ArrayList<>(theBot.commands.values()),
        new BotCommandScopeAllChatAdministrators(),
        null));
  }

  private static final Map<Long, String> TRUSTED_CHATS = Map.of(
      -1001636629132L, "Прожарка бота",
      -1001640782633L, "АНА Онлайн");
  private static final String[] NO_ARGS = new String[0];
  private final String bottoken;
  private final String botname = "anarucomalfabot";
  private final Pattern ptrn = Pattern.compile("^/(\\w+)(?:@" + botname + ")?(.+)?$");
  private final LinkedHashMap<String, BotCommandReaction> commands = new LinkedHashMap<>(0, 1F);

  private Bot(String bottoken) {
    this.bottoken = strippedNotEmpty(bottoken, "bottoken");
    List<BotCommandReaction> commands = List.of(
        new BotCommandReaction("1"    , "Что такое АНА?"             , new ReplaceWithMarkdownResourceReaction("1_what_is"      )),
        new BotCommandReaction("2"    , "Утверждение цели АНА"       , new ReplaceWithMarkdownResourceReaction("2_the_goal"     )),
        new BotCommandReaction("3"    , "12 шагов АНА"               , new ReplaceWithMarkdownResourceReaction("3_steps"        )),
        new BotCommandReaction("4"    , "12 традиций АНА"            , new ReplaceWithMarkdownResourceReaction("4_traditions"   )),
        new BotCommandReaction("5"    , "Выход есть"                 , new ReplaceWithMarkdownResourceReaction("5_solution"     )),
        new BotCommandReaction("6"    , "Приглашаем бога"            , new ReplaceWithMarkdownResourceReaction("6_prey_opening" )),
        new BotCommandReaction("7"    , "Молитва о душевном покое"   , new ReplaceWithMarkdownResourceReaction("7_prey_serenity")),
        new BotCommandReaction("8"    , "Ссылки"                     , new BotReactionChain(List.of(
                                                                       new DeleteCommandMessageReaction       (                 ),
                                                                       new SendMarkdownResourceReaction       ("8_links"        ),
                                                                       new SendMarkdownResourceReaction       ("9_ads"          )))),
        new BotCommandReaction("help" , "Покажу, что могу (но позже)", new SendMarkdownResourceReaction       ("help"           )),
        new BotCommandReaction("start", "Включи меня"                , new SendMarkdownResourceReaction       ("start"          )));
    for (BotCommandReaction i : commands) {
      Object existing = this.commands.put(i.getCommand(), i);
      if (existing != null) {
        throw new IllegalStateException("Another registered for " + i);
      }
    }
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
    if (update.hasMessage()) {
      boolean cared = false;
      final Message msg = update.getMessage();
      final Chat chat = msg.getChat();
      if (msg.isCommand() && TRUSTED_CHATS.containsKey(chat.getId())) {
        assert msg.hasText() : "since msg.isCommand() == true";
        String text = msg.getText();
        Matcher mchr = ptrn.matcher(text);
        if (mchr.matches()) {
          final String cmd = mchr.group(1);
          String tail = mchr.group(2);
          final String[] args = tail == null ? NO_ARGS : tail.trim().split("\\s+");
          final BotCommandReaction reaction = commands.get(cmd);
          if (reaction != null) {
            reaction.received(this, msg.getFrom(), chat, msg, cmd, args);
            cared = true;
          }
        }
        if (!cared) {
          log.warn(String.format(
              "onUpdateReceived: command was not taken care: %s sent to %s msg %s",
              Stringers.toString(msg.getFrom()),
              Stringers.toString(chat),
              msg.getText()));
        }
      } else {
        if (msg.isCommand()) {
          log.warn(String.format(
              "onUpdateReceived: received from untrusted chat: %s sent to %s msg %s",
              Stringers.toString(msg.getFrom()),
              Stringers.toString(chat),
              Stringers.first(128).of(msg.getText())));
        }
      }
    } else {
      log.warn("onUpdateReceived: no message in update: {}", update);
    }
  }
}
