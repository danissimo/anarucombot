package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static online.twelvesteps.anarucombot.BotCommandReaction.checkCodeLegal;

class CommandReactionBuilder<C extends CommandExecutionContext> {
  protected BiFunction<IOException, String, byte[]> defaultBinaryContentSupplier() {
    return (ex, name) -> {
      throw new AssertionError("No default content for " + name, ex);
    };
  }

  protected Function<C, byte[]> binaryResourceContentLazyLoader(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    return ctx -> new BinaryResourceContentSupplier(name, ifCantLoad).get();
  }

  protected Function<C, String> stringSupplier(final Function<C, byte[]> binary, final Charset charset) {
    return ctx -> new String(binary.apply(ctx), charset);
  }

  protected Function<C, String> utf8StringResourceContentLazyLoaderOrDefaultContent(String name) {
    return stringSupplier(
        binaryResourceContentLazyLoader(name, defaultBinaryContentSupplier()),
        StandardCharsets.UTF_8);
  }

  protected SendMessageReaction<C> sendMessage() {
    return new SendMessageReaction<>();
  }

  protected SendChatActionReaction<C> sendChatAction() {
    return new SendChatActionReaction<>();
  }

  protected DeleteMessageReaction<C> deleteMessage() {
    return new DeleteMessageReaction<>();
  }

  protected BotReactionsChain<C> chain(
      BotReaction<?, C> first,
      BotReaction<?, C> second) {
    return chain(first, second, (BotReaction<?, C>[])null);
  }

  @SafeVarargs
  protected final BotReactionsChain<C> chain(
      BotReaction<?, C> first,
      BotReaction<?, C> second,
      BotReaction<?, C>... rest) {
    return new BotReactionsChain<>(first, second, rest);
  }

  protected BotCommandReaction<C> bind(
      String commandCode,
      BotReaction<?, C> reaction) {
    return bind(commandCode, null, reaction);
  }

  protected BotCommandReaction<C> bind(
      String commandCode,
      String commandDescription,
      BotReaction<?, C> reaction) {
    checkCodeLegal(commandCode, "commandCode");
    Object existing = reactions.put(commandCode, reaction);
    if (existing != null) {
      throw new IllegalStateException("Already bound: " + commandCode);
    }
    BotCommandReaction<C> result = new BotCommandReaction<>(commandCode, commandDescription, reaction);
    if (commandDescription != null) {
      commandsWithDescriptions.add(result.command());
    }
    return result;
  }

  private final HashMap<String, BotReaction<?, C>> reactions = new HashMap<>();
  private final ArrayList<BotCommand> commandsWithDescriptions = new ArrayList<>();

  public List<BotCommand> commandsWithDescription() {
    commandsWithDescriptions.trimToSize();
    return commandsWithDescriptions;
  }

  public Map<String, BotReaction<?, C>> reactions() {
    return reactions;
  }
}
