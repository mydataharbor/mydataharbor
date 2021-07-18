package mydataharbor.creator;

import mydataharbor.classutil.classresolver.TypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ConstructorAndArgsConfig implements Serializable {
  /**
   * 构造函数参数个数
   */
  private int argsCount;
  /**
   * 构造器名称
   */
  private String constructorName;
  /**
   * 构造器类型列表
   */
  private List<String> argsType;

  /**
   * 构造器类型参数列表
   */
  private List<TypeInfo> argsTypeInfo;
}