package online.twelvesteps.anarucombot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public // TODO: drop me
final class Stringers {
  private Stringers() {}

  public static String strippedNotEmpty(String s, String argName) {
    checkNotNull(s, "%s = null", argName);
    checkArgument(!s.isEmpty(), "%s empty", argName);
    checkArgument(s.length() == s.strip().length(), "%s not stripped", argName);
    return s;
  }

  private static String orEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String name(String first, String last) {
    first = orEmpty(first);
    last  = orEmpty(last );
    return
        !first.isEmpty() && !last.isEmpty()
        ? first + ' ' + last
        : !first.isEmpty() ? first
        : !last .isEmpty() ? last
        : "";
  }

  public static String stringify(User user) {
    return toString(user);
  }

  public static String toString(User user) {
    if (user == null) {
      return  null;
    }
    String username = orEmpty(user.getUserName());
    String name = name(user.getFirstName(), user.getLastName());
    return "U#" + user.getId()
        + (username.isEmpty() ? "" : '@' + username  )
        + (    name.isEmpty() ? "" : '[' + name + ']');
  }

  private static final String CODE_PRIVATE    = "private"   ;
  private static final String CODE_GROUP      = "group"     ;
  private static final String CODE_SUPERGROUP = "supergroup";
  private static final String CODE_CHANNEL    = "channel"   ;

  public static String stringify(Chat chat) {
    return toString(chat);
  }

  public static String toString(Chat chat) {
    if (chat == null) {
      return null;
    }
    String type = switch (chat.getType()) {
      case CODE_PRIVATE    -> "PR#";
      case CODE_GROUP      -> "GR#";
      case CODE_SUPERGROUP -> "SG#";
      case CODE_CHANNEL    -> "CH#";
      default              -> "!unexpected: " + chat.getType() + '#';
    };
    String title    = orEmpty(chat.getTitle());
    String username = orEmpty(chat.getUserName());
    String name     = name(chat.getFirstName(), chat.getLastName());
    return type + chat.getId()
        + (username.isEmpty() ? "" : '@' + username   )
        + (    name.isEmpty() ? "" : '[' + name  + ']')
        + (   title.isEmpty() ? "" : '[' + title + ']');
  }

  public static String stringify(Collection<User> users) {
    return toString(users);
  }

  public static String toString(Collection<User> users) {
    return users == null ? null : users.stream().map(Stringers::stringify).toList().toString();
  }

  public static String stringify(ChatMember msg) {
    return toString(msg);
  }

  public static String toString(ChatMember msg) {
    return toString(msg.getUser()) + msg.getStatus();
  }

  public static String stringify(CallbackQuery query) {
    return toString(query);
  }

  public static String toString(CallbackQuery query) {
    return "Q#" + query.getId() + '[' + query.getData() + ']';
  }

  public static String toString(InlineKeyboardButton btn) {
    return btn == null ? null : "BTN#" + btn.getCallbackData() + '[' + btn.getText() + ']';
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
