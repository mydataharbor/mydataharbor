package mydataharbor.classutil.test;

import lombok.Data;

import java.util.List;

/**
 * @auth xulang
 * @Date 2021/6/22
 **/
@Data
public class MyClass extends BaseClass1 {

  public MyClass(Integer arg1) {
    this.myClassIntField = arg1;
  }

  private int myClassIntField;

  private List<Character> myClassListCharField;
}
