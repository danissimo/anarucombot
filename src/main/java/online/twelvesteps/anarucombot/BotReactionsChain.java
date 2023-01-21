package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class BotReactionsChain<C extends CommandExecutionContext>
extends BotReaction<BotReactionsChain<C>, C> {
  private final List<BotReaction<?, C>> chain;

  @SafeVarargs
  public BotReactionsChain(
      BotReaction<?, C> first,
      BotReaction<?, C> second,
      BotReaction<?, C>... rest) {
    if (rest == null || rest.length <= 0) {
      chain = List.of(checkNotNull(first), checkNotNull(second));
    } else {
      chain = new ArrayList<>(rest.length + 2);
      chain.add(checkNotNull(first));
      chain.add(checkNotNull(second));
      for (BotReaction<?, C> i : rest) {
        chain.add(checkNotNull(i));
      }
    }
  }

  @Override
  protected void doReact(C ctx) throws TelegramApiException {
    for (BotReaction<?, C> i : chain) {
      i.react(ctx);
    }
  }
}

