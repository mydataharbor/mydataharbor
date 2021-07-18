package mydataharbor.exception;

/**
 * 可重试异常，抛出此异常表示不提交该数据，如果是批量提交模式下，所有批量数据都将不提交
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
public class ResetException extends RuntimeException {
  public ResetException() {
  }

  public ResetException(String message) {
    super(message);
  }

  public ResetException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResetException(Throwable cause) {
    super(cause);
  }

  public ResetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
