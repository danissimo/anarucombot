package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

final class SendMessageReaction<C extends CommandExecutionContext>
extends BotReaction<SendMessageReaction<C>, C> {
  private Supplier<String> text = () -> null;
  private boolean markdown;
  private boolean noLinkPreview;

  public SendMessageReaction<C> text(Supplier<String> text) {
    this.text = checkNotNull(text);
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

  protected void doReact(CommandExecutionContext ctx) throws TelegramApiException {
    SendMessage reply = new SendMessage();
    reply.setChatId(ctx.getUpdate().getMessage().getChatId());
    reply.setText(text.get());
    reply.enableMarkdown(markdown);
    reply.setDisableWebPagePreview(noLinkPreview);
    ctx.getCommunicatingAgent().execute(reply);
  }
}

