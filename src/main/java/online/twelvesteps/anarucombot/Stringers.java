package online.twelvesteps.anarucombot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.google.common.base.Preconditions.checkArgument;

final class Stringers {
  private Stringers() {}

  public static String toString(User user) {
    return user == null ? null : "U#" + user.getId() + '@' + user.getUserName();
  }

  public static String toString(Chat chat) {
    return chat == null ? null : "CH#" + chat.getId();
  }

  public static String toString(InlineKeyboardButton btn) {
    return btn == null ? null : "BTN#" + btn.getCallbackData() + ':' + btn.getText();
  }

  public static FirstNofString first(int n) {
    checkArgument(n >= 0, "n = %s", n);
    return new FirstNofString(n);
  }

  public static final class FirstNofString {
    private final int n;
    private FirstNofString(int n) { this.n = n; }
    public String of(String s) { return s == null ? null : s.substring(0, Integer.min(n, s.length())); }
  }
}
