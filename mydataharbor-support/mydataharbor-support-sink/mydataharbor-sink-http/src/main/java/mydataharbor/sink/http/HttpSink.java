package mydataharbor.sink.http;

import mydataharbor.IDataSink;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @auth xulang
 * @Date 2021/7/6
 **/
@Slf4j
public class HttpSink implements IDataSink<HttpSinkEntity, BaseSettingContext> {

  private HttpSinkConfig httpSinkConfig;

  private OkHttpClient okHttpClient;

  public HttpSink(HttpSinkConfig httpSinkConfig) {
    this.httpSinkConfig = httpSinkConfig;
    ConnectionPool connectionPool = new ConnectionPool(httpSinkConfig.getMaxIdle(), httpSinkConfig.getKeepAliveDuration(), TimeUnit.SECONDS);
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    //包含header、body数据
    loggingInterceptor.setLevel(httpSinkConfig.getLogLevel());
    //在build OkHttpClient的时候加入Log拦截器
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    //设置连接超时
    builder.connectTimeout(httpSinkConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
      //设置读超时
      .readTimeout(httpSinkConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
      //设置写超时
      .writeTimeout(httpSinkConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)
      //是否自动重连
      .retryOnConnectionFailure(true)
      .connectionPool(connectionPool);
    builder.addInterceptor(loggingInterceptor);
    this.okHttpClient = builder.build();
  }

  @Override
  public String name() {
    return "http请求写入器";
  }

  @Override
  public WriterResult write(HttpSinkEntity record, BaseSettingContext settingContext) throws ResetException {
    Request.Builder requestBuilder = new Request.Builder()
      .url(httpSinkConfig.getUrl());
    switch (record.getHttpMethod()) {
      case GET:
        requestBuilder.get();
        break;
      case POST:
        requestBuilder.post(record.getRequestBody());
        break;
      case DELETE:
        if (record.getRequestBody() == null)
          requestBuilder.delete();
        else
          requestBuilder.delete(record.getRequestBody());
        break;
      case PUT:
        requestBuilder.put(record.getRequestBody());
        break;
      case HEAD:
        requestBuilder.head();
        break;
      case PATCH:
        requestBuilder.patch(record.getRequestBody());
        break;
    }
    if (record.getHeaders() != null) {
      for (Map.Entry<String, String> entry : record.getHeaders().entrySet()) {
        requestBuilder.addHeader(entry.getKey(), entry.getValue());
      }
    }
    try {
      Response execute = okHttpClient.newCall(requestBuilder.build()).execute();
    } catch (IOException e) {
      if (e instanceof ConnectException) {
        throw new ResetException("网络连接异常");
      }
      log.error("请求失败！:{}", record);
      return WriterResult.builder().commit(true).success(false).msg(e.getMessage()).build();
    }
    return WriterResult.builder().commit(true).success(true).msg("http request success！").build();
  }

  @Override
  public WriterResult write(List<HttpSinkEntity> records, BaseSettingContext settingContext) throws ResetException {
    for (HttpSinkEntity record : records) {
      write(record, settingContext);
    }
    return WriterResult.builder().commit(true).success(true).msg("http request success！").build();
  }

  @Override
  public void close() throws IOException {

  }
}
