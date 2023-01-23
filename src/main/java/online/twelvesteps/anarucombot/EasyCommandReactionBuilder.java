package online.twelvesteps.anarucombot;

import lombok.val;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

abstract class EasyCommandReactionBuilder<Ctx extends EasyExecutionContext>
extends CommandReactionBuilder<Ctx> {
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
  protected Function<Ctx, byte[]> binaryResourceContentLazyLoader(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    return super.binaryResourceContentLazyLoader(resourceRoot + name + ".md", ifCantLoad);
  }

  SendMessageReaction<Ctx> resource(String name) {
    return sendMessage()
        .text(utf8StringResourceContentLazyLoaderOrDefaultContent(name))
        .markdown()
        .noLinkPreview();
  }

  private static final ScheduledExecutorService TIMER
      = Executors.newScheduledThreadPool(1,
      new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger();
        @Override public Thread newThread(Runnable r) {
          Thread t = new Thread(r, "timer#" + threadNumber.getAndIncrement());
          t.setDaemon(true);
          return t;
        }
      });
  private final long THRESHOLD = 5_000;

  BotReaction<?, Ctx, Void> replaceWith(BotReaction<?, Ctx, ?> reaction) {
    Function<Ctx, String> text = ctx -> {
      val lastReactedAt = ctx.getLastCommandReactedAt().get();
      val sinceLastReacted = System.currentTimeMillis() - lastReactedAt;
      val leftToWaitMillis = THRESHOLD - sinceLastReacted;
      var leftToWaitSecs = (int)Math.ceil(leftToWaitMillis / 1000F);
      leftToWaitSecs = Integer.max(1, leftToWaitSecs);
      String leftToWaitStr = switch (leftToWaitSecs) {
        case 1 -> "секунду";
        case 2 -> "пару секунд";
        case 3, 4 -> leftToWaitSecs + " секунды";
        default -> leftToWaitSecs + " секунд";
      };
      return format("_Не так быстро, я не железный.\nОбратись через %s_.", leftToWaitStr);
    };

    val faulty = sendMessage()
        .replyToReceivedMessage()
        .text(text)
        .markdown()
        .logFailure();

    val successive = chain(
        reaction.logFailure(),
        deleteMessage().logFailure());

    return this.<Void>customReaction()
        .body((final Ctx ctx) -> {
          val lastReactedAt = ctx.getLastCommandReactedAt();
          val sinceLastReacted = System.currentTimeMillis() - lastReactedAt.get();
          if (sinceLastReacted >= THRESHOLD) {
            successive.react(ctx);
            lastReactedAt.set(System.currentTimeMillis());
          } else {
            val msg = faulty.react(ctx);
            if (msg != null) {
              val commandId = ctx.getUpdate().getMessage().getMessageId();
              val replyId = msg.getMessageId();
              val cleanup = chain(
                  deleteMessage().msgId(ignore -> commandId).logFailure(),
                  deleteMessage().msgId(ignore -> replyId).logFailure());
              TIMER.schedule(() -> cleanup.react(ctx), 4_000, MILLISECONDS);
            }
          }
          return null;
        });
  }

  BotReaction<?, Ctx, Void> replaceWith(String name) {
    return replaceWith(resource(name));
  }

  Consumer<Ctx> clearTimestamp() {
    return ctx -> ctx.getLastCommandReactedAt().set(0);
  }
}
