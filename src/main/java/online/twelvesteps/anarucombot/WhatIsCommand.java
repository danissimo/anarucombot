package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
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

import static java.lang.String.format;

@Slf4j
final class WhatIsCommand extends BotCommand {
  private static final WhatIsCommand INSTANCE = new WhatIsCommand();
  private static final String ANSWER;
  static {
    String resourceName = WhatIsCommand.class.getSimpleName() + ".md";
    try (InputStream is = WhatIsCommand.class.getResourceAsStream(resourceName)) {
      if (is == null) {
        throw new BadDeploymentError("Resource not found: " + resourceName);
      }
      ANSWER = new Scanner(is, StandardCharsets.UTF_8).useDelimiter(Pattern.compile("$")).next();
      System.out.println("----------------------------------------------------------------");
      System.out.println(ANSWER);
      System.out.println("----------------------------------------------------------------");
    } catch (IOException ex) {
      throw new BadDeploymentError("Failed to load resource: " + resourceName, ex);
    }
  }

  public static WhatIsCommand instance() {
    return INSTANCE;
  }

  private WhatIsCommand() {
    super("msg_1_what_is", "Что такое АНА?");
  }

  @Override
  public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
    SendMessage answer = new SendMessage();
    answer.enableMarkdown(true);
    answer.setChatId(chat.getId());
    answer.setText(ANSWER);
    try {
      absSender.execute(answer);
    } catch (TelegramApiException ex) {
      log.error(
          format(
              "execute: %s sent to %s msg %s with args %s",
              Stringers.toString(user), Stringers.toString(chat),
              getCommandIdentifier(), Arrays.toString(arguments)),
          ex);
    }
  }
}
