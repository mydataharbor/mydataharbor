package mydataharbor.sink.redis;

import mydataharbor.sink.redis.config.SingleRedisConfig;
import mydataharbor.IDataSink;
import mydataharbor.setting.BaseSettingContext;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

import java.io.IOException;
import java.time.Duration;

/**
 * 单机redis写入
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
public abstract class AbstractSingleRedisSink<R, S extends BaseSettingContext> implements IDataSink<R, S> {

  private final StatefulRedisConnection<String, String> conn;

  private final RedisClient redisClient;

  @Override
  public String name() {
    return "redis单机模式";
  }

  public AbstractSingleRedisSink(SingleRedisConfig redisConfig) {
    RedisURI.Builder builder = RedisURI.builder()
      .withHost(redisConfig.getHost())
      .withPort(redisConfig.getPort())
      .withTimeout(Duration.ofMillis(redisConfig.getTimeout()));
    if (redisConfig.isEnableAuth()) {
      builder.withPassword(redisConfig.getAuth());
    }
    this.redisClient = RedisClient.create(builder.build());
    this.conn = redisClient.connect();
  }


  @Override
  public void close() throws IOException {
    conn.close();
    redisClient.shutdown();
  }

  protected StatefulRedisConnection<String, String> getConn() {
    return conn;
  }

  protected RedisClient getRedisClient() {
    return redisClient;
  }
}
