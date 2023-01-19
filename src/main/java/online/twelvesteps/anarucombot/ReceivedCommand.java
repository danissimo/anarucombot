package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class ReceivedCommand {
  private static final int CODE_MAX_LEN = 31; // mind the leading slash

  private final String code;
  private final BotReaction reaction;

  public ReceivedCommand(String code, BotReaction reaction) {
    checkNotNull(code, "code = null");
    checkArgument(!code.isEmpty(), "code empty");
    checkArgument(code.length() == code.strip().length(), "code not stripped");
    checkArgument(code.length() <= CODE_MAX_LEN, "code too long; max = %s", CODE_MAX_LEN);
    this.code = code;
    this.reaction = checkNotNull(reaction, "reaction = null");
  }

  public String code() {
    return code;
  }

  public BotReaction reaction() {
    return reaction;
  }

  public void received(AbsSender sendingBackAgent, User fromUser, Chat fromChat, String... args) {
    reaction().react(sendingBackAgent, fromUser, fromChat, code(), args);
  }
}
