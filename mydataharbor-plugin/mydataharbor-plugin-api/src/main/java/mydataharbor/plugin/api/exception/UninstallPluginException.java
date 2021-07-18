package mydataharbor.plugin.api.exception;

/**
 * @auth xulang
 * @Date 2021/6/25
 **/
public class UninstallPluginException extends RuntimeException{
  public UninstallPluginException() {
  }

  public UninstallPluginException(String message) {
    super(message);
  }

  public UninstallPluginException(String message, Throwable cause) {
    super(message, cause);
  }

  public UninstallPluginException(Throwable cause) {
    super(cause);
  }

  public UninstallPluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
