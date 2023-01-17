package mydataharbor.converter.protocol;

import lombok.Data;
import mydataharbor.IProtocolData;

/**
 * 原样数据输出
 * Created by xulang on 2021/8/10.
 */
@Data
public class OriginalProtocolData<T> implements IProtocolData {

  public OriginalProtocolData(T record) {
    this.record = record;
  }

  private T record;

  @Override
  public String protocolName() {
    return "原样数据输出";
  }
}
