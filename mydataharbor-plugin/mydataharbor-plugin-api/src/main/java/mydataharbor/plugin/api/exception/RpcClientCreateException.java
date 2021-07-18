package mydataharbor.plugin.api.exception;

/**
 * @auth xulang
 * @Date 2021/6/17
 **/
public class RpcClientCreateException extends Exception {
  public RpcClientCreateException() {
  }

  public RpcClientCreateException(String message) {
    super(message);
  }

  public RpcClientCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public RpcClientCreateException(Throwable cause) {
    super(cause);
  }

  public RpcClientCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
