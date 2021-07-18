package mydataharbor.sink.http;

import mydataharbor.classutil.classresolver.FieldMarker;
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
@FieldMarker("httpsink配置")
public class HttpSinkConfig {

  @FieldMarker(value = "需要请求的url地址", require = true)
  private String url;

  @FieldMarker(value = "连接超时时间", require = false)
  private Integer connectTimeout = 1000;

  @FieldMarker(value = "读超时时间", require = false)
  private Integer readTimeout = 5000;

  @FieldMarker(value = "写超时时间", require = false)
  private Integer writeTimeout = 5000;

  @FieldMarker(value = "最大空闲连接数", require = false)
  private Integer maxIdle = 100;

  @FieldMarker(value = "存活时间" ,des = "单位：秒", require = false)
  private Integer keepAliveDuration = 30;

  @FieldMarker(value = "请求打印日志等级", require = false)
  private HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.BASIC;

}
