package mydataharbor.web.exception;

/**
 * Created by xulang on 2021/8/26.
 */
public class ReconfigException extends Exception {
  public ReconfigException() {
  }

  public ReconfigException(String message) {
    super(message);
  }

  public ReconfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReconfigException(Throwable cause) {
    super(cause);
  }

  public ReconfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
