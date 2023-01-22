package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

final class DeleteMessageReaction<C extends CommandExecutionContext>
extends BotReaction<DeleteMessageReaction<C>, C> {
  @Override
  protected void doReact(C ctx)
  throws TelegramApiException {
    Message msg = ctx.getUpdate().getMessage();
    Chat chat = msg.getChat();
    ctx.getCommunicatingAgent()
        .execute(
            new DeleteMessage(
                String.valueOf(chat.getId()),
                msg.getMessageId()));
  }
}

