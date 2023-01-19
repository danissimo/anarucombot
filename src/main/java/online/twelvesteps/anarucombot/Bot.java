package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
final class Bot extends TelegramLongPollingBot {
  public static void main(String... args) throws Exception {
    new TelegramBotsApi(DefaultBotSession.class).registerBot(new Bot());
  }

  private static final int COMMAND_CODE_MAX_LEN = 31; // mind the leading slash
  private static final Map<Long, String> TRUSTED_CHATS = Map.of(
      -1001636629132L, "Прожарка бота",
      -1001640782633L, "АНА Онлайн");
  private static final String[] NO_ARGS = new String[0];
  private static final String ENV_TOKEN = "ANARUCOMBOTTOKEN";
  private final String bottoken;
  private final String botname = "anarucombot";
  private final Pattern ptrn = Pattern.compile("^/(\\w+)(?:@" + botname + ")?(.+)?$");
  private final Map<String, BotReaction> commands = new HashMap<>(0, 1F);

  private Bot() {
    System.out.println("Started on behalf of " + botname);
    bottoken = System.getenv(ENV_TOKEN);
    {
      String errLine
          = bottoken == null ? "No " + ENV_TOKEN + " environment variable value found"
          : bottoken.isEmpty() ? ENV_TOKEN + " environment variable is blank"
          : bottoken.length() != bottoken.strip().length() ? ENV_TOKEN + " environment variable is not stripped"
          : null;
      if (errLine != null) {
        System.err.println(errLine
            + "\nUsage: " + ENV_TOKEN + "=\"$TOKEN\" java -jar anarucombot.jar");
        System.exit(-1);
      }
    }

    bindReaction(new SendMarkdownResourceReaction("start"          ), "start");
    bindReaction(new SendMarkdownResourceReaction("help"           ), "help" );
    bindReaction(new SendMarkdownResourceReaction("1_what_is"      ), "1", "msg_1_what_is"      );
    bindReaction(new SendMarkdownResourceReaction("2_the_goal"     ), "2", "msg_2_the_goal"     );
    bindReaction(new SendMarkdownResourceReaction("3_steps"        ), "3", "msg_3_steps"        );
    bindReaction(new SendMarkdownResourceReaction("4_traditions"   ), "4", "msg_4_traditions"   );
    bindReaction(new SendMarkdownResourceReaction("5_solution"     ), "5", "msg_5_solution"     );
    bindReaction(new SendMarkdownResourceReaction("6_prey_opening" ), "6", "msg_6_prey_opening" );
    bindReaction(new SendMarkdownResourceReaction("7_prey_serenity"), "7", "msg_7_prey_serenity");
    bindReaction(BotReactionChain.builder()
            .chain(new SendMarkdownResourceReaction("8_links"))
            .chain(new SendMarkdownResourceReaction("9_ads"))
            .build(), "8", "msg_8_links");
  }

  private void bindReaction(BotReaction reaction, String... toCmdCodes) {
    for (String i : toCmdCodes) {
      throwIfBadCommandCode(i);
      Object existing = commands.put(i, reaction);
      if (existing != null) {
        throw new IllegalStateException("Another registered for " + i);
      }
    }
  }

  private static void throwIfBadCommandCode(String commandCode) {
    checkNotNull(commandCode, "commandCode = null");
    checkArgument(!commandCode.isEmpty(), "commandCode empty");
    checkArgument(commandCode.length() == commandCode.strip().length(), "commandCode not stripped");
    checkArgument(commandCode.length() <= COMMAND_CODE_MAX_LEN, "commandCode too long; max = %s", COMMAND_CODE_MAX_LEN);
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
          final BotReaction reaction = commands.get(cmd);
          if (reaction != null) {
            reaction.react(this, msg.getFrom(), chat, cmd, args);
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
        String what = msg.isCommand() ? "command" : "message";
        log.warn(String.format(
            "onUpdateReceived: %s was not taken care: %s sent to %s msg %s",
            what,
            Stringers.toString(msg.getFrom()),
            Stringers.toString(chat),
            Stringers.first(128).of(msg.getText())));
      }
    } else {
      log.warn("onUpdateReceived: no message in update: {}", update);
    }
  }
}
