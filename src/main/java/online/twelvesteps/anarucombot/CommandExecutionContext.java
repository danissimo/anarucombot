package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

class CommandExecutionContext {
  private static final String[] NO_ARGS = new String[0];
  private final AbsSender communicatingAgent;
  private final Pattern ptrn;
  private Update update;
  private String receivedCommand;
  private String receivedTargetBotname;
  private String[] receivedCommandArgs;

  public CommandExecutionContext(AbsSender communicatingAgent) {
    this.communicatingAgent = checkNotNull(communicatingAgent, "communicatingAgent = null");
    ptrn = Pattern.compile("^/(\\w+)(?:@(\\w+))?(?:\\s+(.+))?$");
  }

  public void update(Update update) {
    this.update = update;
    this.receivedCommand = null;
    this.receivedTargetBotname = null;
    this.receivedCommandArgs = null;

    if (update.hasMessage() && update.getMessage().hasText()) {
      Matcher mchr = ptrn.matcher(update.getMessage().getText());
      if (mchr.matches()) {
        this.receivedCommand = mchr.group(1);
        this.receivedTargetBotname = mchr.group(2);
        String tail = mchr.group(3);
        this.receivedCommandArgs = tail == null ? NO_ARGS : tail.trim().split("\\s+");
      }
    }
  }

  public AbsSender getCommunicatingAgent() { return communicatingAgent; }
  public Update getUpdate() { return update; }
  public String getReceivedCommand() { return receivedCommand; }
  public String getReceivedTargetBotname() { return receivedTargetBotname; }
  public String[] getReceivedCommandArgs() { return receivedCommandArgs; }
}
