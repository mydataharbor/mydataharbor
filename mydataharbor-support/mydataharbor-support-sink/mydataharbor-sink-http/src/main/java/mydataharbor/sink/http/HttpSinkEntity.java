package mydataharbor.sink.http;

import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.RequestBody;

import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/7/6
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpSinkEntity {

  @MyDataHarborMarker(title = "请求方法", require = true)
  private HttpMethod httpMethod;

  @MyDataHarborMarker(title = "请求体", des = "有些请求不需要时，置为null", require = false)
  private RequestBody requestBody;

  @MyDataHarborMarker(title = "头信息", require = true)
  private Map<String, String> headers;

}
