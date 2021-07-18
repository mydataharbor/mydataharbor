package mydataharbor.exception;

/**
 * @auth xulang
 * @Date 2021/6/19
 **/
public class InstanceCreateException extends Exception {
  public InstanceCreateException() {
  }

  public InstanceCreateException(String message) {
    super(message);
  }

  public InstanceCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public InstanceCreateException(Throwable cause) {
    super(cause);
  }

  public InstanceCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
