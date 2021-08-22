package mydataharbor.source.kafka.protocal;

import lombok.AllArgsConstructor;
import lombok.Data;
import mydataharbor.IProtocalData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xulang on 2021/8/22.
 */
@Data
public class KafkaProtocalData implements IProtocalData {

  private String topic;

  private String key;

  private String value;

  private Map<String, byte[]> header = new HashMap<>();

  @Override
  public String protocalName() {
    return "kafka-protocal-data";
  }

}
