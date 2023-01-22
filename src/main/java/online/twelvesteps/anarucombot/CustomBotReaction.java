package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.google.common.base.Preconditions.checkNotNull;

final class CustomBotReaction<Ctx extends CommandExecutionContext, Result>
extends BotReaction<CustomBotReaction<Ctx, Result>, Ctx, Result> {
  public interface BotReactionBody<Ctx, Result> {
    Result react(Ctx ctx) throws TelegramApiException;
  }

  private BotReactionBody<Ctx, Result> body = ctx -> null;

  public CustomBotReaction<Ctx, Result> body(BotReactionBody<Ctx, Result> body) {
    this.body = checkNotNull(body);
    return self();
  }

  @Override
  protected Result doReact(Ctx ctx) throws TelegramApiException {
    return body.react(ctx);
  }
}
