package mydataharbor.sink.redis.config;

import mydataharbor.classutil.classresolver.MyDataHarborMarker;
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

  @MyDataHarborMarker(title = "redis地址")
  private String host;

  @MyDataHarborMarker(title = "redis端口")
  private int port;

  @Builder.Default
  @MyDataHarborMarker(title = "是否启用授权")
  private boolean enableAuth = false;

  @MyDataHarborMarker(title = "授权信息")
  private String auth;

  @MyDataHarborMarker(title = "写入超时时间")
  private long timeout;
}
