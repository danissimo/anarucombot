package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class DeleteMessageReaction<Ctx extends CommandExecutionContext>
extends BotReaction<DeleteMessageReaction<Ctx>, Ctx, Boolean> {
  private Function<Ctx, String> chatId = ctx -> String.valueOf(ctx.getUpdate().getMessage().getChat().getId());
  private Function<Ctx, Integer> msgId = ctx -> ctx.getUpdate().getMessage().getMessageId();

  public DeleteMessageReaction<Ctx> chatId(Function<Ctx, String> chatId) {
    this.chatId = checkNotNull(chatId);
    return self();
  }

  public DeleteMessageReaction<Ctx> msgId(Function<Ctx, Integer> msgId) {
    this.msgId = checkNotNull(msgId);
    return self();
  }

  @Override
  protected Boolean doReact(Ctx ctx) throws TelegramApiException {
    return ctx.getCommunicatingAgent().execute(
        new DeleteMessage(chatId.apply(ctx), msgId.apply(ctx)));
  }
}

