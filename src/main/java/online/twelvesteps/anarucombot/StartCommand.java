package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
final class StartCommand extends BotCommand {
  private static final StartCommand INSTANCE = new StartCommand();

  public static StartCommand instance() {
    return INSTANCE;
  }

  private StartCommand() {
    super("/start", "Уже включён");
  }

  @Override
  public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
    log.info("execute: {} sent to {} {} with {}", user, chat, getCommandIdentifier(), arguments);
  }
}
