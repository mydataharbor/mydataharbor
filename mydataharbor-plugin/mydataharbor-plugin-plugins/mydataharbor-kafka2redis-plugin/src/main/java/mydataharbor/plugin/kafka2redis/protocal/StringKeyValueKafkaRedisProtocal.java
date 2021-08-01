package mydataharbor.plugin.kafka2redis.protocal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mydataharbor.IProtocalData;

/**
 * kafka-redis
 * 字符串key value
 *
 * @auth xulang
 * @Date 2021/5/6
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StringKeyValueKafkaRedisProtocal implements IProtocalData {

  @Override
  public String protocalName() {
    return "kafka->redis:string-key-value";
  }

  private String key;

  private String value;

  private OPT opt;

  public static enum OPT {
    /**
     * 新增或修改
     */
    UPDATE_OR_INSERT,

    /**
     * 删除
     */
    DELETE
  }

}
