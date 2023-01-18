package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

abstract class BotCommand {
  private static final int MAXLEN = 31; // mind the leading slash

  private final String id;

  protected BotCommand(String id) {
    checkNotNull(id, "id = null");
    checkArgument(!id.isEmpty(), "id empty");
    checkArgument(id.length() == id.strip().length(), "id not stripped");
    checkArgument(id.length() <= MAXLEN, "id too long; max = %s", MAXLEN);
    this.id = id;
  }

  public String id() {
    return id;
  }

  protected abstract void received(AbsSender sendingBackAgent, User fromUser, Chat fromChat, String... args);
}
