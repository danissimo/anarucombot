package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

final class SendChatActionReaction<C extends CommandExecutionContext>
extends BotReaction<SendChatActionReaction<C>, C> {
  @Override
  protected void doReact(C ctx) throws TelegramApiException {
    SendChatAction reply = new SendChatAction();
    reply.setChatId(String.valueOf(ctx.getUpdate().getMessage().getChat()));
    reply.setAction(ActionType.TYPING);
    ctx.getCommunicatingAgent().execute(reply);
  }
}
