package mydataharbor.classutil.classresolver;

import lombok.Data;

import java.io.Serializable;

@Data
public class FieldInfo extends TypeInfo implements Serializable {

  public FieldInfo() {

  }

  public FieldInfo(String fieldName) {
    this.fieldName = fieldName;
  }

  public FieldInfo(String fieldName, String title) {
    this.fieldName = fieldName;
    this.title = title;
  }

  /**
   * 属性名称
   */
  private String fieldName;

  /**
   * 标题
   */
  private String title;

  /**
   * 注释
   */
  private String des;

  /**
   * 是否必须
   */
  private boolean require;


}