package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
final class SendMarkdownResourceReaction extends BotReaction {
  private final String name;

  public SendMarkdownResourceReaction(String name) {
    checkNotNull(name, "name = null");
    checkArgument(!name.isEmpty(), "name empty");
    checkArgument(name.length() == name.strip().length(), "name not stripped");
    this.name = name;
  }

  private String loadResourceContent() {
    String resource = name + ".md";
    try (InputStream is = getClass().getResourceAsStream(resource)) {
      if (is == null) {
        throw new BadDeploymentError("Resource not found: " + resource);
      }
      return new Scanner(is, StandardCharsets.UTF_8).useDelimiter(Pattern.compile("$")).next();
    } catch (IOException ex) {
      throw new BadDeploymentError("Failed to load resource: " + resource, ex);
    }
  }

  @Override
  public void react(AbsSender sendingBackAgent, User fromUser, Chat fromChat, Message msg, String cmd, String... args) {
    SendMessage reply = new SendMessage();
    reply.setChatId(fromChat.getId());
    reply.enableMarkdown(true);
    reply.setText(loadResourceContent());
    reply.disableWebPagePreview();
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
