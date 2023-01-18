package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

@Slf4j
final class SendFileContentCommand extends BotCommand {
  private final String file;

  public SendFileContentCommand(String id) {
    super(id);
    String resourceName = id + ".md";
    try (InputStream is = getClass().getResourceAsStream(resourceName)) {
      if (is == null) {
        throw new BadDeploymentError("Resource not found: " + resourceName);
      }
      file = new Scanner(is, StandardCharsets.UTF_8).useDelimiter(Pattern.compile("$")).next();
    } catch (IOException ex) {
      throw new BadDeploymentError("Failed to load resource: " + resourceName, ex);
    }
  }

  @Override
  public void received(AbsSender sendingBackAgent, User fromUser, Chat fromChat, String... args) {
    SendMessage reply = new SendMessage();
    reply.setChatId(fromChat.getId());
    reply.enableMarkdown(true);
    reply.setText(file);
    reply.disableWebPagePreview();
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
