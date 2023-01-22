package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

final class SendChatActionReaction<Ctx extends CommandExecutionContext>
extends BotReaction<SendChatActionReaction<Ctx>, Ctx, Boolean> {
  @Override
  protected Boolean doReact(Ctx ctx) throws TelegramApiException {
    SendChatAction reply = new SendChatAction();
    reply.setChatId(String.valueOf(ctx.getUpdate().getMessage().getChat()));
    reply.setAction(ActionType.TYPING);
    return ctx.getCommunicatingAgent().execute(reply);
  }
}
