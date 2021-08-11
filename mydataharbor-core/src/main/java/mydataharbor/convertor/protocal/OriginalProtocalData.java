package mydataharbor.convertor.protocal;

import lombok.Data;
import mydataharbor.IProtocalData;

/**
 * 原样数据输出
 * Created by xulang on 2021/8/10.
 */
@Data
public class OriginalProtocalData<T> implements IProtocalData {

  public OriginalProtocalData(T record) {
    this.record = record;
  }

  private T record;

  @Override
  public String protocalName() {
    return null;
  }
}
