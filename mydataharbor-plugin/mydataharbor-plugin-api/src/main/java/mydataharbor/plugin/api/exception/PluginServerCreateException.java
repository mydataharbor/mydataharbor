package mydataharbor.plugin.api.exception;

/**
 * pluginserver创建异常
 *
 * @auth xulang
 * @Date 2021/6/11
 **/
public class PluginServerCreateException extends RuntimeException {

  public PluginServerCreateException() {
  }

  public PluginServerCreateException(String message) {
    super(message);
  }

  public PluginServerCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginServerCreateException(Throwable cause) {
    super(cause);
  }

  public PluginServerCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
