package mydataharbor.exception;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/
public class DataSinkCommonException extends RuntimeException {
  public DataSinkCommonException() {
  }

  public DataSinkCommonException(String message) {
    super(message);
  }

  public DataSinkCommonException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataSinkCommonException(Throwable cause) {
    super(cause);
  }

  public DataSinkCommonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
