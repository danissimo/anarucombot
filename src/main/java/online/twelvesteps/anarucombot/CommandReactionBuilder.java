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

class CommandReactionBuilder<Ctx extends CommandExecutionContext> {
  protected BiFunction<IOException, String, byte[]> defaultBinaryContentSupplier() {
    return (ex, name) -> {
      throw new AssertionError("No default content for " + name, ex);
    };
  }

  protected Function<Ctx, byte[]> binaryResourceContentLazyLoader(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    return ctx -> new BinaryResourceContentSupplier(name, ifCantLoad).get();
  }

  protected Function<Ctx, String> stringSupplier(
      final Function<Ctx, byte[]> binary,
      final Charset charset) {
    return ctx -> new String(binary.apply(ctx), charset);
  }

  protected Function<Ctx, String> utf8StringResourceContentLazyLoaderOrDefaultContent(String name) {
    return stringSupplier(
        binaryResourceContentLazyLoader(name, defaultBinaryContentSupplier()),
        StandardCharsets.UTF_8);
  }

  protected <Result> CustomBotReaction<Ctx, Result> customReaction() {
    return new CustomBotReaction<>();
  }

  protected SendMessageReaction<Ctx> sendMessage() {
    return new SendMessageReaction<>();
  }

  protected SendChatActionReaction<Ctx> sendChatAction() {
    return new SendChatActionReaction<>();
  }

  protected DeleteMessageReaction<Ctx> deleteMessage() {
    return new DeleteMessageReaction<>();
  }

  protected BotReactionsChain<Ctx> chain() {
    return new BotReactionsChain<>();
  }

  protected BotReactionsChain<Ctx> chain(
      BotReaction<?, Ctx, ?> first,
      BotReaction<?, Ctx, ?> second) {
    return chain(first, second, (BotReaction<?, Ctx, ?>[])null);
  }

  @SafeVarargs
  protected final BotReactionsChain<Ctx> chain(
      BotReaction<?, Ctx, ?> first,
      BotReaction<?, Ctx, ?> second,
      BotReaction<?, Ctx, ?>... rest) {
    return new BotReactionsChain<>(first, second, rest);
  }

  protected BotCommandReaction<Ctx> bind(
      String commandCode,
      BotReaction<?, Ctx, ?> reaction) {
    return bind(commandCode, null, reaction);
  }

  protected BotCommandReaction<Ctx> bind(
      String commandCode,
      String commandDescription,
      BotReaction<?, Ctx, ?> reaction) {
    checkCodeLegal(commandCode, "commandCode");
    Object existing = reactions.put(commandCode, reaction);
    if (existing != null) {
      throw new IllegalStateException("Already bound: " + commandCode);
    }
    BotCommandReaction<Ctx> result = new BotCommandReaction<>(commandCode, commandDescription, reaction);
    if (commandDescription != null) {
      commandsWithDescriptions.add(result.command());
    }
    return result;
  }

  private final HashMap<String, BotReaction<?, Ctx, ?>> reactions = new HashMap<>();
  private final ArrayList<BotCommand> commandsWithDescriptions = new ArrayList<>();

  public List<BotCommand> commandsWithDescription() {
    commandsWithDescriptions.trimToSize();
    return commandsWithDescriptions;
  }

  public Map<String, BotReaction<?, Ctx, ?>> reactions() {
    return reactions;
  }
}
