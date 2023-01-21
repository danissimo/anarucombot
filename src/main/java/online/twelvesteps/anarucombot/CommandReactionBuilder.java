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
import java.util.function.Supplier;

import static online.twelvesteps.anarucombot.BotCommandReaction.checkCodeLegal;

abstract class CommandReactionBuilder<C extends CommandExecutionContext> {
  protected abstract BiFunction<IOException, String, byte[]> defaultBinaryContentSupplier();

  protected Supplier<byte[]> binaryResourceContentLazyLoader(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    return new BinaryResourceContentSupplier(name, ifCantLoad);
  }

  protected Supplier<String> stringSupplier(final Supplier<byte[]> binary, final Charset charset) {
    return () -> new String(binary.get(), charset);
  }

  protected Supplier<String> utf8StringResourceContentLazyLoaderOrDefaultContent(String name) {
    return stringSupplier(
        binaryResourceContentLazyLoader(name, defaultBinaryContentSupplier()),
        StandardCharsets.UTF_8);
  }

  protected SendMessageReaction<C> sendMessage() {
    return new SendMessageReaction<>();
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
