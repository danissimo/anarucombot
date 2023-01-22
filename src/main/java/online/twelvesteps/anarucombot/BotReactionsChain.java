package online.twelvesteps.anarucombot;

import lombok.val;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class BotReactionsChain<Ctx extends CommandExecutionContext>
extends BotReaction<BotReactionsChain<Ctx>, Ctx, List<Object>> {
  private final List<BotReaction<?, Ctx, ?>> chain;

  public BotReactionsChain() {
    chain = new ArrayList<>(2);
  }

  public BotReactionsChain(BotReaction<?, Ctx, ?> first) {
    this(first, null);
  }

  @SafeVarargs
  public BotReactionsChain(
      BotReaction<?, Ctx, ?> first,
      BotReaction<?, Ctx, ?> second,
      BotReaction<?, Ctx, ?>... rest) {
    this();
    checkNotNull(first, "first = null");
    chain.add(first);
    if (second == null) {
      checkArgument(rest == null, "rest != null while second == null");
    } else {
      chain.add(second);
      if (rest != null) {
        for (val i : rest) {
          chain.add(checkNotNull(i, "an element of rest is null"));
        }
      }
    }
  }

  public BotReactionsChain<Ctx> chain(BotReaction<?, Ctx, ?> reaction) {
    chain.add(reaction);
    return self();
  }

  @Override
  protected List<Object> doReact(Ctx ctx) throws TelegramApiException {
    val result = new ArrayList<>(chain.size());
    for (BotReaction<?, Ctx, ?> i : chain) {
      result.add(i.react(ctx));
    }
    return result;
  }
}
