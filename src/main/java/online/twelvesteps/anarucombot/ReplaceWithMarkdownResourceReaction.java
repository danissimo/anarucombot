package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

final class ReplaceWithMarkdownResourceReaction extends BotReaction {
  private final SendMarkdownResourceReaction sendMarkdownResourceReaction;
  private final DeleteCommandMessageReaction deleteCommandMessageReaction;

  public ReplaceWithMarkdownResourceReaction(String name) {
    sendMarkdownResourceReaction = new SendMarkdownResourceReaction(name);
    deleteCommandMessageReaction = new DeleteCommandMessageReaction();
  }

  @Override
  public void react(AbsSender sendingBackAgent, User fromUser, Chat fromChat, Message msg, String cmd, String... args) {
    sendMarkdownResourceReaction.react(sendingBackAgent, fromUser, fromChat, msg, cmd, args);
    deleteCommandMessageReaction.react(sendingBackAgent, fromUser, fromChat, msg, cmd, args);
  }
}
