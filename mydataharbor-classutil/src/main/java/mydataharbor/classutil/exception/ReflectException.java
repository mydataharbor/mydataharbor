package mydataharbor.classutil.exception;

/**
 * 反射异常
 *
 * @auth xulang
 * @Date 2021/6/22
 **/
public class ReflectException extends RuntimeException {
  public ReflectException() {
  }

  public ReflectException(String message) {
    super(message);
  }

  public ReflectException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReflectException(Throwable cause) {
    super(cause);
  }

  public ReflectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
