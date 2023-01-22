package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.concurrent.atomic.AtomicLong;

final class EasyExecutionContext extends CommandExecutionContext {
  private final AtomicLong lastCommandReactedAt = new AtomicLong();

  public EasyExecutionContext(AbsSender communicatingAgent) {
    super(communicatingAgent);
  }

  public AtomicLong getLastCommandReactedAt() {
    return lastCommandReactedAt;
  }
}
