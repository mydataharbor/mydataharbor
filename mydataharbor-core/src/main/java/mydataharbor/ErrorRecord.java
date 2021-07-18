package mydataharbor;

import lombok.Builder;
import lombok.Data;

/**
 * 处理过程中错误的数据
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
@Data
@Builder
public class ErrorRecord<T, E> {
  private T record;
  /**
   * 可识别的错误
   */
  private E knownError;

  /**
   * 未知异常
   */
  private Exception unknownException;
}
