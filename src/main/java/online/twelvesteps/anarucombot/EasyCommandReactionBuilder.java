package online.twelvesteps.anarucombot;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class EasyCommandReactionBuilder<C extends EasyExecutionContext>
extends CommandReactionBuilder<C> {
  @Override
  protected BiFunction<IOException, String, byte[]> defaultBinaryContentSupplier() {
    return (ex, name) -> {
      assert name != null;
      if (ex == null) {
        throw new BadDeploymentError("Resource not found: " + name);
      } else {
        throw new BadDeploymentError("Failed to load resource: " + name, ex);
      }
    };
  }

  //private final String resourceRoot = '/' + getClass().getPackageName().replace('.', '/') + '/';
  private static final String resourceRoot = "";

  @Override
  protected Supplier<byte[]> binaryResourceContentLazyLoader(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    return super.binaryResourceContentLazyLoader(resourceRoot + name + ".md", ifCantLoad);
  }

  BotReaction<?, C> resource(String name) {
    return sendMessage()
        .text(utf8StringResourceContentLazyLoaderOrDefaultContent(name))
        .markdown()
          .noLinkPreview();
  }

  BotReaction<?, C> replaceWith(BotReaction<?, C> reaction) {
    return chain(
        reaction.quiet()
            .reactIf(ctx -> System.currentTimeMillis() - ctx.lastCommandReactedAt() >= 10_000)
            .ifReacted(EasyExecutionContext::lastCommandReactedNow),
        deleteMessage().quiet());
  }

  BotReaction<?, C> replaceWith(String name) {
    return replaceWith(resource(name));
  }

  Consumer<C> clearTimestamp() {
    return ctx -> ctx.lastCommandReactedAt(0);
  }
}
