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
    R extends BotReaction<R, C>,
    C extends CommandExecutionContext> {
  private Predicate<C> reactIf = ctc -> true;
  private Consumer<C> ifReacted = ctx -> {};
  private boolean swallowAndLog;

  protected R self() {
    @SuppressWarnings("unchecked")
    R self = (R)this;
    return self;
  }

  public R reactIf(Predicate<C> predicate) {
    this.reactIf = checkNotNull(predicate);
    return self();
  }

  public R swallowAndLog() {
    swallowAndLog = true;
    return self();
  }

  public R ifReacted(Consumer<C> ifReacted) {
    this.ifReacted = checkNotNull(ifReacted);
    return self();
  }

  public void react(C ctx) throws TelegramApiException {
    if (reactIf.test(ctx)) {
      try {
        doReact(ctx);
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
  }

  protected abstract void doReact(C ctx) throws TelegramApiException;
}
