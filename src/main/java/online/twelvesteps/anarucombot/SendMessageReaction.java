package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class SendMessageReaction<C extends CommandExecutionContext>
extends BotReaction<SendMessageReaction<C>, C> {
  private Function<C, String> text = ctx -> null;
  private boolean replyToReceivedMessage;
  private boolean markdown;
  private boolean noLinkPreview;

  public SendMessageReaction<C> text(Function<C, String> text) {
    this.text = checkNotNull(text);
    return self();
  }

  public SendMessageReaction<C> replyToReceivedMessage() {
    replyToReceivedMessage = true;
    return self();
  }

  public SendMessageReaction<C> markdown() {
    markdown = true;
    return self();
  }

  public SendMessageReaction<C> noLinkPreview() {
    noLinkPreview = true;
    return self();
  }

  protected void doReact(C ctx) throws TelegramApiException {
    SendMessage msg = new SendMessage();
    msg.setChatId(ctx.getUpdate().getMessage().getChatId());
    if (replyToReceivedMessage) {
      msg.setReplyToMessageId(ctx.getUpdate().getMessage().getMessageId());
    }
    msg.setText(text.apply(ctx));
    msg.enableMarkdown(markdown);
    msg.setDisableWebPagePreview(noLinkPreview);
    ctx.getCommunicatingAgent().execute(msg);
  }
}

