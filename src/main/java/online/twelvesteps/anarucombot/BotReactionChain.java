package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class BotReactionChain extends BotReaction {
  private final List<BotReaction> chain;

  public static Builder builder() {
    return new Builder();
  }

  public BotReactionChain(List<BotReaction> chain) {
    this.chain = checkNotNull(chain, "chain = null");
  }

  public BotReactionChain chain(BotReaction cmd) {
    chain.add(checkNotNull(cmd, "cmd = null"));
    return this;
  }

  @Override
  protected void react(
      AbsSender sendingBackAgent,
      User fromUser, Chat fromChat,
      Message msg, String cmd,
      String... args) {
    for (BotReaction i : chain) {
      i.react(sendingBackAgent, fromUser, fromChat, msg, cmd, args);
    }
  }

  public static final class Builder {
    private final List<BotReaction> chain = new ArrayList<>(2);

    private Builder() {}

    public Builder chain(BotReaction cmd) {
      chain.add(checkNotNull(cmd, "cmd = null"));
      return this;
    }

    public BotReactionChain build() {
      return new BotReactionChain(chain);
    }
  }
}
