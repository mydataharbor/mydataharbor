package mydataharbor.plugin.kafka.to.redis;

import mydataharbor.pipline.kafka2redis.creator.Kafka2RedisCreator;
import mydataharbor.classutil.classresolver.FieldMarker;
import mydataharbor.plugin.base.util.JsonUtil;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * @auth xulang
 * @Date 2021/5/16
 **/
@Extension
@FieldMarker(value = "kafka到redis创建器")
public class Kafka2RedisPiplineCreator extends Kafka2RedisCreator implements ExtensionPoint {

  public Kafka2RedisPiplineCreator() {

  }

  @Override
  public <T> T parseJson(String json, Class<T> clazz) {
    return JsonUtil.jsonToObject(json, clazz);
  }
}
