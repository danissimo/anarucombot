package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static online.twelvesteps.anarucombot.Stringers.strippedNotEmpty;

final class BotCommandReaction<C extends CommandExecutionContext> {
  private static final int COMMAND_CODE_MAX_LEN = 31; // mind the leading slash

  private final String commandCode;
  private final BotCommand command;
  private final BotReaction<?, C> reaction;

  public static String checkCodeLegal(String code, String argName) {
    checkNotNull(code, "%s = null", argName);
    checkArgument(!code.isEmpty(), "%s empty", argName);
    checkArgument(code.length() == code.strip().length(), "%s not stripped", argName);
    checkArgument(code.length() <= COMMAND_CODE_MAX_LEN, "%s too long; max = %s", argName, COMMAND_CODE_MAX_LEN);
    return code;
  }

  public BotCommandReaction(
      String commandCode,
      String commandDescription,
      BotReaction<?, C> reaction) {
    this.commandCode = checkCodeLegal(commandCode, "commandCode");
    command
        = commandDescription == null ? null
        : new BotCommand(commandCode, strippedNotEmpty(commandDescription, "commandDescription"));
    this.reaction = checkNotNull(reaction);
  }

  public String commandCode() {
    return commandCode;
  }

  public BotCommand command() {
    return command;
  }

  public BotReaction<?, C> reaction() {
    return reaction;
  }
}
