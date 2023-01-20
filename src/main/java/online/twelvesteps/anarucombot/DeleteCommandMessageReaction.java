package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

@Slf4j
final class DeleteCommandMessageReaction extends BotReaction {
  @Override
  public void react(AbsSender sendingBackAgent, User fromUser, Chat fromChat, Message msg, String cmd, String... args) {
    DeleteMessage reply = new DeleteMessage(String.valueOf(fromChat.getId()), msg.getMessageId());
    try {
      sendingBackAgent.execute(reply);
    } catch (TelegramApiException ex) {
      log.error(String.format(
          "react: %s sent to %s msg %s with args %s",
          Stringers.toString(fromUser), Stringers.toString(fromChat), cmd, Arrays.toString(args)),
          ex);
    }
  }
}
