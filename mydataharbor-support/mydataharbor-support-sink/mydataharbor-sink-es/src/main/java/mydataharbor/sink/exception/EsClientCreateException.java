package mydataharbor.sink.exception;

/**
 * Created by xulang on 2021/7/27.
 */
public class EsClientCreateException extends RuntimeException{
  public EsClientCreateException() {
  }

  public EsClientCreateException(String message) {
    super(message);
  }

  public EsClientCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public EsClientCreateException(Throwable cause) {
    super(cause);
  }

  public EsClientCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
