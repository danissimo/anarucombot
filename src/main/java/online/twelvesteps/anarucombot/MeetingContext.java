package online.twelvesteps.anarucombot;

import java.util.List;

final class MeetingContext {
  private final List<String> steps = List.of(
      "Что такое АНА?",
      "Утверждение цели АНА",
      "12 шагов АНА",
      "12 традиций АНА",
      "Выход есть",
      "Молитва–приглашение бога",
      "Молитва о душевном покое",
      "Конец"
  );
  private int curr;

  boolean prev() {
    return curr > 0;
  }

  String next() {
    return steps.get(curr);
  }

  void forward() {
    // TODO: protect
    curr += 1;
  }

  void backward() {
    // TODO: protect
    curr -= 1;
  }
}
