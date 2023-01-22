package online.twelvesteps.anarucombot;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

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
  protected Function<C, byte[]> binaryResourceContentLazyLoader(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    return super.binaryResourceContentLazyLoader(resourceRoot + name + ".md", ifCantLoad);
  }

  BotReaction<?, C> resource(String name) {
    return sendMessage()
        .text(utf8StringResourceContentLazyLoaderOrDefaultContent(name))
        .markdown()
        .noLinkPreview();
  }

  private final long THRESHOLD = 5_000;

  BotReaction<?, C> replaceWith(BotReaction<?, C> reaction) {
    return chain(
        sendMessage()
            .replyToReceivedMessage()
            .text(ctx -> {
              long leftToWaitMillis = ctx.getLastCommandReactedAt().get() + THRESHOLD - System.currentTimeMillis();
              int leftToWaitSecs = (int)Math.ceil(leftToWaitMillis / 1000F);
              String leftToWaitStr = switch (leftToWaitSecs) {
                case 1 -> "секунду";
                case 2 -> "пару секунд";
                case 3, 4 -> leftToWaitSecs + " секунды";
                default -> leftToWaitSecs + " секунд";
              };
              return format("_Не так быстро, я не железный. Обратись снова через %s_", leftToWaitStr);
            })
            .markdown()
            .reactIf(ctx -> System.currentTimeMillis() - ctx.getLastCommandReactedAt().get() < THRESHOLD),
        chain(
            reaction.swallowAndLog(),
            deleteMessage().swallowAndLog())
            .reactIf(ctx -> System.currentTimeMillis() - ctx.getLastCommandReactedAt().get() >= THRESHOLD)
            .ifReacted(ctx -> ctx.getLastCommandReactedAt().set(System.currentTimeMillis())));
  }

  BotReaction<?, C> replaceWith(String name) {
    return replaceWith(resource(name));
  }

  Consumer<C> clearTimestamp() {
    return ctx -> ctx.getLastCommandReactedAt().set(0);
  }
}
