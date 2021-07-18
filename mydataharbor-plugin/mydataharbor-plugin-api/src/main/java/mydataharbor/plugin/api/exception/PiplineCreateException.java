package mydataharbor.plugin.api.exception;

/**
 * pipline创建异常
 *
 * @auth xulang
 * @Date 2021/6/23
 **/
public class PiplineCreateException extends RuntimeException {
  public PiplineCreateException() {
  }

  public PiplineCreateException(String message) {
    super(message);
  }

  public PiplineCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public PiplineCreateException(Throwable cause) {
    super(cause);
  }

  public PiplineCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
