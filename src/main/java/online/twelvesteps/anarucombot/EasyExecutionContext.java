package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.bots.AbsSender;

final class EasyExecutionContext extends CommandExecutionContext {
  private long lastCommandReactedAt;

  public EasyExecutionContext(AbsSender communicatingAgent) {
    super(communicatingAgent);
  }

  public long lastCommandReactedAt() {
    return lastCommandReactedAt;
  }

  public void lastCommandReactedAt(long lastCommandReactedAt) {
    this.lastCommandReactedAt = lastCommandReactedAt;
  }

  public void lastCommandReactedNow() {
    lastCommandReactedAt(System.currentTimeMillis());
  }
}
