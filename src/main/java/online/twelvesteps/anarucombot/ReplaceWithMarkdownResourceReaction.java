package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

final class ReplaceWithMarkdownResourceReaction extends BotReaction {
  private final DeleteCommandMessageReaction deleteCommandMessageReaction;
  private final SendMarkdownResourceReaction sendMarkdownResourceReaction;

  public ReplaceWithMarkdownResourceReaction(String name) {
    deleteCommandMessageReaction = new DeleteCommandMessageReaction();
    sendMarkdownResourceReaction = new SendMarkdownResourceReaction(name);
  }

  @Override
  public void react(AbsSender sendingBackAgent, User fromUser, Chat fromChat, Message msg, String cmd, String... args) {
    deleteCommandMessageReaction.react(sendingBackAgent, fromUser, fromChat, msg, cmd, args);
    sendMarkdownResourceReaction.react(sendingBackAgent, fromUser, fromChat, msg, cmd, args);
  }
}
