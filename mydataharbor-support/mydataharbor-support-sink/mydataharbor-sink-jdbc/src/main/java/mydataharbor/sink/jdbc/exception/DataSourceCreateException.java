package mydataharbor.sink.jdbc.exception;

/**
 * Created by xulang on 2021/8/18.
 */
public class DataSourceCreateException extends RuntimeException{
  public DataSourceCreateException() {
  }

  public DataSourceCreateException(String message) {
    super(message);
  }

  public DataSourceCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataSourceCreateException(Throwable cause) {
    super(cause);
  }

  public DataSourceCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
