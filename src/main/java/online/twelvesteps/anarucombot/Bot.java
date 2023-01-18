package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
final class Bot extends TelegramLongPollingBot {
  public static void main(String... args) throws Exception {
    new TelegramBotsApi(DefaultBotSession.class).registerBot(new Bot());
  }

  private static final Map<Long, String> TRUSTED_CHATS = Map.of(
      -1001636629132L, "Прожарка бота",
      -1001640782633L, "АНА Онлайн");

  private static final String[] NO_ARGS = new String[0];
  private final String botname = "anarucombot";
  private final Pattern ptrn = Pattern.compile("^/(\\w+)(?:@" + botname + ")?(.+)?$");
  private final String bottoken;

  private final Map<String, BotCommand> commands = new HashMap<>(0, 1F);

  private Bot() {
    bottoken = System.getenv("ANARUCOMBOTTOKEN");
    {
      String errLine
          = bottoken == null ? "No ANARUCOMBOTTOKEN environment variable value found"
          : bottoken.isEmpty() ? "ANARUCOMBOTTOKEN environment variable is blank"
          : bottoken.length() != bottoken.strip().length() ? "ANARUCOMBOTTOKEN environment variable is not stripped"
          : null;
      if (errLine != null) {
        System.err.println(errLine
            + "\nUsage: ANARUCOMBOTTOKEN=\"$TOKEN\" java -jar anarucombot.jar");
        System.exit(-1);
      }
    }

    registerCommands(
        new SendFileContentCommand("start"),
        new SendFileContentCommand("help"),
        new SendFileContentCommand("msg_1_what_is"),
        new SendFileContentCommand("msg_2_the_goal"),
        new SendFileContentCommand("msg_3_steps"),
        new SendFileContentCommand("msg_4_traditions"),
        new SendFileContentCommand("msg_5_solution"),
        new SendFileContentCommand("msg_6_prey_opening"),
        new SendFileContentCommand("msg_7_prey_serenity"),
        new CommandChain("msg_8_links")
            .chain(new SendFileContentCommand("msg_8_links"))
            .chain(new SendFileContentCommand("msg_9_ads")),
        new BeginCommand()
    );
  }

  private void registerCommand(BotCommand cmd) {
    Object existing = commands.put(cmd.id(), cmd);
    if (existing != null) {
      throw new IllegalStateException("Another registered: " + cmd.id());
    }
  }

  private void registerCommands(BotCommand... cmds) {
    for (BotCommand i : cmds) {
      registerCommand(i);
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
          String id = mchr.group(1);
          String tail = mchr.group(2);
          String[] args = tail == null ? NO_ARGS : tail.trim().split("\\s+");
          BotCommand cmd = commands.get(id);
          if (cmd != null) {
            log.info(String.format(
                "onUpdateReceived: %s sent to %s msg %s",
                Stringers.toString(msg.getFrom()),
                Stringers.toString(chat),
                Stringers.first(128).of(msg.getText())));
            cmd.received(this, msg.getFrom(), chat, args);
            cared = true;
          }
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
      if (!cared) {
        log.warn(String.format(
            "onUpdateReceived: update was not taken care: %s sent to %s msg %s",
            Stringers.toString(msg.getFrom()),
            Stringers.toString(chat),
            Stringers.first(128).of(msg.getText())));
      }
    } else if (update.hasCallbackQuery()) {
      // TODO: set up a context on /begin

      // cb.message with the callback button that originated the query. Note
      //   that message content and message date will not be available if the
      //   message is too old
      // cb.data contains what was set in callbackData on the markup keyboard
      CallbackQuery cb = update.getCallbackQuery();
      Message msg = cb.getMessage();
      val replyMarkup = msg.getReplyMarkup();
      msg.setFrom(null);
      msg.setChat(null);
      msg.setReplyMarkup(null);
      log.info("onUpdateReceived:"
          + " cb.data = {}, cb.msg.text = {}",
          cb.getData(), msg.getText());
      for (List<InlineKeyboardButton> i : replyMarkup.getKeyboard()) {
        log.info("cb.msg.replyMarkup: {}", String.join(" | ", i.stream().map(Stringers::toString).toArray(String[]::new)));
      }
    } else {
      log.warn("onUpdateReceived: no message in update: {}", update);
    }
  }
}
