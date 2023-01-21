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
  private Predicate<C> predicate = ctc -> true;
  private Consumer<C> signaller = ctx -> {};
  private boolean quiet;

  protected R self() {
    @SuppressWarnings("unchecked")
    R self = (R)this;
    return self;
  }

  public R reactIf(Predicate<C> predicate) {
    this.predicate = checkNotNull(predicate);
    return self();
  }

  public R reactAlways() {
    this.predicate = ctx -> true;
    return self();
  }

  public R quiet() {
    quiet = true;
    return self();
  }

  public R ifReacted(Consumer<C> signaller) {
    this.signaller = checkNotNull(signaller);
    return self();
  }

  public void react(C ctx) throws TelegramApiException {
    if (predicate.test(ctx)) {
      try {
        doReact(ctx);
      } catch (TelegramApiException ex) {
        if (!quiet) {
          throw ex;
        }
        final Message msg = ctx.getUpdate().getMessage();
        log.error(format("react on [%s] sent to %s by %s",
            msg.getText(),
            stringify(msg.getChat()),
            stringify(msg.getFrom())),
            ex);
      }
      signaller.accept(ctx);
    }
  }

  protected abstract void doReact(C ctx) throws TelegramApiException;
}
