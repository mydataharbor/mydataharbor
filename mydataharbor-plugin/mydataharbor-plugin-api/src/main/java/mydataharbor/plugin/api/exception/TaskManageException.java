package mydataharbor.plugin.api.exception;

/**
 * @auth xulang
 * @Date 2021/6/23
 **/
public class TaskManageException extends RuntimeException {
  public TaskManageException() {
  }

  public TaskManageException(String message) {
    super(message);
  }

  public TaskManageException(String message, Throwable cause) {
    super(message, cause);
  }

  public TaskManageException(Throwable cause) {
    super(cause);
  }

  public TaskManageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
