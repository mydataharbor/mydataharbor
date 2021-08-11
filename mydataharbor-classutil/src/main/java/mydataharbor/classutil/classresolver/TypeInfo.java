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


