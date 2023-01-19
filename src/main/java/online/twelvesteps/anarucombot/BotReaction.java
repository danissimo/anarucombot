package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

abstract class BotReaction {
  protected abstract void react(AbsSender sendingBackAgent, User fromUser, Chat fromChat, String cmd, String... args);
}
