package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

@Slf4j
final class BeginCommand extends BotCommand {
  public BeginCommand() {
    super("begin");
  }

  private final MeetingContext ctx = new MeetingContext();

  @Override
  protected void received(AbsSender sendingBackAgent, User fromUser, Chat fromChat, String... args) {
    val next = InlineKeyboardButton.builder()
        .text(ctx.next()).callbackData("next")
        .build();
    val keyboard = InlineKeyboardMarkup.builder()
        .keyboardRow(List.of(next))
        .build();
    val reply = SendMessage.builder()
        .chatId(fromChat.getId())
        .text("Поехали")
        .replyMarkup(keyboard)
        .build();
    try {
      sendingBackAgent.execute(reply);
    } catch (TelegramApiException ex) {
      log.error(String.format(
              "received: %s sent to %s msg %s with args %s",
              Stringers.toString(fromUser), Stringers.toString(fromChat), id(), Arrays.toString(args)),
          ex);
    }
  }
}
