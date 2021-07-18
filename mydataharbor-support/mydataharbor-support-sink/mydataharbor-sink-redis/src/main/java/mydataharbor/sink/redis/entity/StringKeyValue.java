package mydataharbor.sink.redis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字符串keyvalue实现
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StringKeyValue {
  private String key;
  private String value;
}
