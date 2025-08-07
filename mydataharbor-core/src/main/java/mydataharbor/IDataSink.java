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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 数据写入器
 *
 * @auth xulang
 * @Date 2021/4/29
 **/
public interface IDataSink<R, S extends BaseSettingContext> extends Closeable, IData {
    /**
     * 在执行线程里调用，插件可以在此方法中初始化一些变量
     * 包括但不限于从永久存储里获取任务之前执行的状态数据等
     * @param settingContext
     */
    default void init(S settingContext){}

  /**
   * sink名称
   *
   * @return
   */
  String name();

  /**
   * 单条写入，当该接口抛出任何异常时，都不会提交改数据
   * 如果开启多线程处理，单条提交，则需要idatasink的write相关方法线程安全
   *
   * @param record
   * @return
   */
  WriterResult write(R record, S settingContext) throws ResetException;

  /**
   * 批量写入
   * 当该接口抛出任何异常时，改条数据都不会被提交
   *
   * @param records
   * @return
   */
  WriterResult write(List<R> records, S settingContext) throws ResetException;

  default Type getRType() {
    return getTypeByIndex(0, "R", IDataSink.class);
  }

  default Type getSType() {
    return getTypeByIndex(1, "S", IDataSink.class);
  }

  /**
   * 写入结果
   */
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  class WriterResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 是否提交
     */
    private boolean commit;

    /**
     * 写入器返回
     */
    private Object writeReturn;

    /**
     * 失败消息
     */
    private String msg;
  }
}