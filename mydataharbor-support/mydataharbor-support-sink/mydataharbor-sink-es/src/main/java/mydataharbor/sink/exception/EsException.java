package mydataharbor.sink.exception;

/**
 * Created by xulang on 2021/7/27.
 */
public class EsException extends RuntimeException{
  public EsException() {
  }

  public EsException(String message) {
    super(message);
  }

  public EsException(String message, Throwable cause) {
    super(message, cause);
  }

  public EsException(Throwable cause) {
    super(cause);
  }

  public EsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
