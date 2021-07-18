package mydataharbor.classutil.classresolver;

import java.lang.annotation.*;

/**
 * @auth xulang
 * @Date 2021/6/21
 **/
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface FieldMarker {

  /**
   * 描述
   *
   * @return
   */
  String value();

  /**
   * 参数描述
   *
   * @return
   */
  String des() default "";

  /**
   *
   * 是否必须
   * @return
   */
  boolean require() default true;
}
