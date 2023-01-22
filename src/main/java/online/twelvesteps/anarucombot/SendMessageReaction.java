package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class SendMessageReaction<Ctx extends CommandExecutionContext>
extends BotReaction<SendMessageReaction<Ctx>, Ctx, Message> {
  private Function<Ctx, String> text = ctx -> null;
  private boolean replyToReceivedMessage;
  private boolean markdown;
  private boolean noLinkPreview;

  public SendMessageReaction<Ctx> text(Function<Ctx, String> text) {
    this.text = checkNotNull(text);
    return self();
  }

  public SendMessageReaction<Ctx> replyToReceivedMessage() {
    replyToReceivedMessage = true;
    return self();
  }

  public SendMessageReaction<Ctx> markdown() {
    markdown = true;
    return self();
  }

  public SendMessageReaction<Ctx> noLinkPreview() {
    noLinkPreview = true;
    return self();
  }

  protected Message doReact(Ctx ctx) throws TelegramApiException {
    SendMessage msg = new SendMessage();
    msg.setChatId(ctx.getUpdate().getMessage().getChatId());
    if (replyToReceivedMessage) {
      msg.setReplyToMessageId(ctx.getUpdate().getMessage().getMessageId());
    }
    msg.setText(text.apply(ctx));
    msg.enableMarkdown(markdown);
    msg.setDisableWebPagePreview(noLinkPreview);
    return ctx.getCommunicatingAgent().execute(msg);
  }
}

