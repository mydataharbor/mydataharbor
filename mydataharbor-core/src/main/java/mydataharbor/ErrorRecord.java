/*
 * 版权所有 (C) [2020] [xulang 1053618636@qq.com]
 *
 * 此程序是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证第3版或
 * （根据您的选择）任何更高版本重新分发和/或修改它。
 *
 * 此程序基于希望它有用而分发，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。详见 GNU 通用公共许可证。
 *
 * 您应该已经收到 GNU 通用公共许可证的副本。如果没有，请参阅
 * <http://www.gnu.org/licenses/>.
 *
 */


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