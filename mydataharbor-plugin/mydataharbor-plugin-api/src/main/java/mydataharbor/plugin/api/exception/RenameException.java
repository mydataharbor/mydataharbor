package mydataharbor.plugin.api.exception;

/**
 * @auth xulang
 * @Date 2021/6/24
 **/
public class RenameException extends Exception{
  public RenameException() {
  }

  public RenameException(String message) {
    super(message);
  }

  public RenameException(String message, Throwable cause) {
    super(message, cause);
  }

  public RenameException(Throwable cause) {
    super(cause);
  }

  public RenameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
