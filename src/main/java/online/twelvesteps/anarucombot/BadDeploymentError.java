package online.twelvesteps.anarucombot;

final class BadDeploymentError extends Error {
  public BadDeploymentError(String message) {
    super(message);
  }

  public BadDeploymentError(String message, Throwable cause) {
    super(message, cause);
  }
}
