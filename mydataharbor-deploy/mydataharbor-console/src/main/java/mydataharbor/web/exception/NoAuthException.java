package mydataharbor.web.exception;

/**
 * Created by xulang on 2021/8/25.
 */
public class NoAuthException extends Exception {
  public NoAuthException() {
  }

  public NoAuthException(String message) {
    super(message);
  }

  public NoAuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoAuthException(Throwable cause) {
    super(cause);
  }

  public NoAuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
