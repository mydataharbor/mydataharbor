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

import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Type;

/**
 * 数据转换器
 *
 * @auth xulang
 * @Date 2021/4/29
 **/
public interface IDataConverter<P extends IProtocolData, R, S extends BaseSettingContext> extends IData {
    /**
     * 在执行线程里调用，插件可以在此方法中初始化一些变量
     * 包括但不限于从永久存储里获取任务之前执行的状态数据等
     * @param settingContext
     */
    default void init(S settingContext){}
  /**
   * 由协议数据转成可被执行的writer数据
   *
   * @return
   */
  R convert(P record, S settingContext) throws ResetException;

  default Type getPType() {
    return getTypeByIndex(0, "P", IDataConverter.class);
  }

  default Type getRType() {
    return getTypeByIndex(1, "R", IDataConverter.class);
  }

  default Type getSType() {
    return getTypeByIndex(2, "S", IDataConverter.class);
  }
}