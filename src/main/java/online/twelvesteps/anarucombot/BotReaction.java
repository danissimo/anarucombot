package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static online.twelvesteps.anarucombot.Stringers.stringify;

@Slf4j
abstract class BotReaction<
    Reaction extends BotReaction<Reaction, Ctx, Result>,
    Ctx extends CommandExecutionContext,
    Result> {
  private Predicate<Ctx> reactIf = ctc -> true;
  private Consumer<Ctx> ifReacted = ctx -> {};
  private boolean swallowAndLog;

  protected Reaction self() {
    @SuppressWarnings("unchecked")
    Reaction self = (Reaction)this;
    return self;
  }

  public Reaction reactIf(Predicate<Ctx> predicate) {
    this.reactIf = checkNotNull(predicate);
    return self();
  }

  public Reaction logFailure() {
    swallowAndLog = true;
    return self();
  }

  public Reaction ifReacted(Consumer<Ctx> ifReacted) {
    this.ifReacted = checkNotNull(ifReacted);
    return self();
  }

  public Result react(Ctx ctx) throws TelegramApiException {
    if (reactIf.test(ctx)) {
      try {
        return doReact(ctx);
      } catch (TelegramApiException ex) {
        if (!swallowAndLog) {
          throw ex;
        }
        final Message msg = ctx.getUpdate().getMessage();
        log.error(format("react on [%s] sent to %s by %s",
            msg.getText(),
            stringify(msg.getChat()),
            stringify(msg.getFrom())),
            ex);
      }
      ifReacted.accept(ctx);
    }
    return null;
  }

  protected abstract Result doReact(Ctx ctx) throws TelegramApiException;
}
