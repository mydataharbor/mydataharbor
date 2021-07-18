package mydataharbor.exception;

/**
 * 结束异常，poll方法中抛出此异常表示数据拉取结束
 *
 * @auth xulang
 * @Date 2021/5/6
 **/
public class TheEndException extends Exception {
  public TheEndException() {
  }

  public TheEndException(String message) {
    super(message);
  }

  public TheEndException(String message, Throwable cause) {
    super(message, cause);
  }

  public TheEndException(Throwable cause) {
    super(cause);
  }

  public TheEndException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
