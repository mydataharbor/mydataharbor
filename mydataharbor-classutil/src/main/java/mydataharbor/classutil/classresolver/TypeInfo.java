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


package mydataharbor.classutil.classresolver;

import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @auth xulang
 * @Date 2021/6/20
 **/
@Data
public class TypeInfo implements Serializable {
  /**
   * 类型
   */
  private String clazzStr;

  /**
   * 对应的类型，不需要序列化
   */
  private transient Type type;

  /**
   * 是否数组
   */
  private boolean array = false;

  /**
   * 基础类型
   */
  private boolean baseType = false;

  /**
   * java基础类型
   */
  private boolean javaBase = false;

  /**
   * 是否是map
   */
  private boolean map = false;

  /**
   * 是否是枚举
   */
  private boolean enumeration = false;

  /**
   * 字段列表
   */
  private List<FieldInfo> fieldInfos;

  public void setJavaBase(boolean javaBase) {
    this.javaBase = javaBase;
    if (javaBase) {
      baseType = true;
    }
  }


}