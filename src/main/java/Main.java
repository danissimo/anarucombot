import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Main {
  public static void main(String... args) throws Exception {
    //String msg = "/8@anarucombot   x yyy   ";
    String msg = "/8";
    Matcher mchr = Pattern.compile("^/(\\w+)(?:@(\\w+))?(?:\\s+(.+))?$").matcher(msg);
    if (mchr.matches()) {
      for (int i = 0; i <= mchr.groupCount(); i++) {
        System.out.printf("%d : [%s]%n", i, mchr.group(i));
      }
    } else {
      System.err.println("No match found");
    }
  }
}
