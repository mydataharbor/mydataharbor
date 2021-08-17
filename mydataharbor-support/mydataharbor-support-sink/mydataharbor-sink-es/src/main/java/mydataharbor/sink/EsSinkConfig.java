package mydataharbor.sink;

import lombok.Data;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;

import java.util.List;

/**
 * Created by xulang on 2021/7/26.
 */
@Data
@MyDataHarborMarker(title = "Es写入器配置")
public class EsSinkConfig {

  @MyDataHarborMarker(title = "es连接ip信息", des = "如：[127.0.0.1:9400,127.0.0.1:9500]")
  private List<String> esIpPort;

  @MyDataHarborMarker(title = "连接超时时间", des = "默认2s", require = false)
  private long connectTimeOut = 2000;

  @MyDataHarborMarker(title = "通讯超时时间", des = "默认5s", require = false)
  private long socketTimeOut = 5000;

  @MyDataHarborMarker(title = "是否需要授权连接", require = false)
  private boolean enableAuth = false;

  @MyDataHarborMarker(title = "用户名", require = false)
  private String userName;

  @MyDataHarborMarker(title = "密码", require = false)
  private String password;

  @MyDataHarborMarker(title = "写入索引配置")
  private WriteIndexConfig writeIndexConfig;

}
