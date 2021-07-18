package mydataharbor.plugin.api.plugin;

import mydataharbor.creator.ClassInfo;
import mydataharbor.classutil.classresolver.TypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @auth xulang
 * @Date 2021/6/20
 **/
@Data
public class DataSinkCreatorInfo implements Serializable {
  /**
   * 类全限定名
   */
  private String clazz;

  /**
   * 创建器类型
   */
  public String type;

  /**
   * 配置全限定名
   */
  private TypeInfo configClassInfo;


  private List<ClassInfo> dataSourceClassInfo;

  private List<ClassInfo> protocalConvertorClassInfo;

  private List<ClassInfo> checkerClassInfo;

  private List<ClassInfo> dataConvertorClassInfo;

  private List<ClassInfo> dataSinkClassInfo;

  private List<ClassInfo> settingClassInfo;


}
