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
 * @author xulang
 * 协议转化器,将dataprovider提供的原始介质数据，转化为可处理的协议
 */
public interface IProtocolDataConverter<T, P extends IProtocolData, S extends BaseSettingContext> extends IData {
    /**
     * 在执行线程里调用，插件可以在此方法中初始化一些变量
     * 包括但不限于从永久存储里获取任务之前执行的状态数据等
     * @param settingContext
     */
    default void init(S settingContext){}
  /**
   * 转化，从原始信息，转成可被处理的协议数据
   *
   * @param record
   * @return
   */
  P convert(T record, S settingContext) throws ResetException;

  default Type getTType() {
    return getTypeByIndex(0, "T", IProtocolDataConverter.class);
  }

  default Type getPType() {
    return getTypeByIndex(1, "P", IProtocolDataConverter.class);
  }

  default Type getSType() {
    return getTypeByIndex(2, "S", IProtocolDataConverter.class);
  }

}