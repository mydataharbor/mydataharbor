package mydataharbor.datasource;

import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import lombok.Data;

/**
 * 限流配置
 *
 * @auth xulang
 * @Date 2021/5/6
 **/

@Data
public class RateLimitConfig {

  @MyDataHarborMarker(title = "限流组", des = "默认系统会设置为taskid，则表示在同jvm下的每个task的总速度，用户也可以自己设置", require = false)
  private String rateGroup;

  @MyDataHarborMarker(title = "限速 /s")
  private Long speed;

}
