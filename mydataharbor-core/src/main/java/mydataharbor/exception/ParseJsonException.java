package mydataharbor.exception;

/**
 * @auth xulang
 * @Date 2021/6/23
 **/
public class ParseJsonException extends RuntimeException {
  public ParseJsonException() {
  }

  public ParseJsonException(String message) {
    super(message);
  }

  public ParseJsonException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParseJsonException(Throwable cause) {
    super(cause);
  }

  public ParseJsonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
