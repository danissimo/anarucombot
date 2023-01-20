package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.google.common.base.Preconditions.checkNotNull;
import static online.twelvesteps.anarucombot.ReceivedBotCommand.checkCodeLegal;

final class BotCommandReaction extends BotCommand {
  private final BotReaction reaction;

  public BotCommandReaction(String code, String dscr, BotReaction reaction) {
    super(checkCodeLegal(code), dscr);
    this.reaction = checkNotNull(reaction);
  }

  public BotReaction reaction() {
    return reaction;
  }

  public void received(
      AbsSender sendingBackAgent,
      User fromUser, Chat fromChat,
      Message msg, String cmd,
      String... args) {
    reaction().react(sendingBackAgent, fromUser, fromChat, msg, cmd, args);
  }
}
