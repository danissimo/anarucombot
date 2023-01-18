package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class CommandChain extends BotCommand {
  private final List<BotCommand> chain = new ArrayList<>(2);

  public CommandChain(String id) {
    super(id);
  }

  public CommandChain chain(BotCommand cmd) {
    chain.add(checkNotNull(cmd, "cmd = null"));
    return this;
  }

  @Override
  protected void received(AbsSender sendingBackAgent, User fromUser, Chat fromChat, String... args) {
    for (BotCommand i : chain) {
      i.received(sendingBackAgent, fromUser, fromChat, args);
    }
  }
}
