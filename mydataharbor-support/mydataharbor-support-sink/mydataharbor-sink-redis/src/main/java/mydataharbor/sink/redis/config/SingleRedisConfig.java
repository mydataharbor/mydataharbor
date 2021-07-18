package mydataharbor.sink.redis.config;

import mydataharbor.classutil.classresolver.FieldMarker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 单机模式下的redis配置
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SingleRedisConfig {

  @FieldMarker(value = "redis地址")
  private String host;

  @FieldMarker(value = "redis端口")
  private int port;

  @Builder.Default
  @FieldMarker(value = "是否启用授权")
  private boolean enableAuth = false;

  @FieldMarker(value = "授权信息")
  private String auth;

  @FieldMarker(value = "写入超时时间")
  private long timeout;
}
