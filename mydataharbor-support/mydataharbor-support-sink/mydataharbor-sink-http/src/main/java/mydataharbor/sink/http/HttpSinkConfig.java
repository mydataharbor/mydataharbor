package mydataharbor.sink.http;

import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @auth xulang
 * @Date 2021/7/6
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@MyDataHarborMarker(title = "httpsink配置")
public class HttpSinkConfig {

  @MyDataHarborMarker(title = "需要请求的url地址", require = true)
  private String url;

  @MyDataHarborMarker(title = "连接超时时间", require = false)
  private Integer connectTimeout = 1000;

  @MyDataHarborMarker(title = "读超时时间", require = false)
  private Integer readTimeout = 5000;

  @MyDataHarborMarker(title = "写超时时间", require = false)
  private Integer writeTimeout = 5000;

  @MyDataHarborMarker(title = "最大空闲连接数", require = false)
  private Integer maxIdle = 100;

  @MyDataHarborMarker(title = "存活时间" ,des = "单位：秒", require = false)
  private Integer keepAliveDuration = 30;

  @MyDataHarborMarker(title = "请求打印日志等级", require = false)
  private HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.BASIC;

}
