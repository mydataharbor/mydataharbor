package mydataharbor.system.plugin.exception;

/**
 * Created by xulang on 2021/9/15.
 */
public class ScriptException extends RuntimeException{
  public ScriptException() {
  }

  public ScriptException(String message) {
    super(message);
  }

  public ScriptException(String message, Throwable cause) {
    super(message, cause);
  }

  public ScriptException(Throwable cause) {
    super(cause);
  }

  public ScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
