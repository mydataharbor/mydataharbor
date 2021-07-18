package mydataharbor.source.kafka;

import mydataharbor.source.kafka.config.SimpleKafkaConfig;
import mydataharbor.datasource.AbstractRateLimitDataSource;
import mydataharbor.datasource.RateLimitConfig;
import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.classutil.classresolver.FieldMarker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * 普通kafka数据源
 *
 * @auth xulang
 * @Date 2021/5/7
 **/
@Slf4j
@FieldMarker(value = "kafka数据源")
public class SimpleKafkaDataSource extends AbstractRateLimitDataSource<ConsumerRecord<String, String>, BaseSettingContext> {

  protected KafkaConsumer<String, String> kafkaConsumer;

  /**
   * 该构造方法暂时不初始化kafkaconsumer
   *
   * @param rateLimitConfig
   */
  public SimpleKafkaDataSource(RateLimitConfig rateLimitConfig) {
    super(rateLimitConfig);
  }

  public SimpleKafkaDataSource(SimpleKafkaConfig simpleKafkaConfig) {
    super(simpleKafkaConfig);
    Properties kafkaConfig = new Properties();
    kafkaConfig.putAll(simpleKafkaConfig.getKafkaConfig());
    kafkaConsumer = new KafkaConsumer<String, String>(kafkaConfig);
    kafkaConsumer.subscribe(simpleKafkaConfig.getTopics(), new ConsumerListener());
  }

  @Override
  public String dataSourceType() {
    return "kafka";
  }


  @Override
  public Collection<ConsumerRecord<String, String>> doPoll(BaseSettingContext settingContext) throws TheEndException {
    //这里为了限流，将ConsumerRecords 转为list
    ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
    List<ConsumerRecord<String, String>> records = new ArrayList<>();
    for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
      records.add(consumerRecord);
    }
    return records;
  }

  @Override
  public void commit(ConsumerRecord<String, String> record, BaseSettingContext settingContext) {
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1L));
    kafkaConsumer.commitSync(offsets);
  }

  @Override
  public void commit(Iterable<ConsumerRecord<String, String>> records, BaseSettingContext baseSettingContext) {
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    for (ConsumerRecord<String, String> record : records) {
      offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1L));
    }

    kafkaConsumer.commitSync(offsets);
  }

  @Override
  public void rollback(ConsumerRecord<String, String> record, BaseSettingContext settingContext) {
    //当前position和record offset取最小值
    TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
    long nowPosition = kafkaConsumer.position(topicPartition);
    long position = Math.min(nowPosition, record.offset());
    kafkaConsumer.seek(topicPartition, position);
    try {
      //防止单条消息循环处理占用CPU
      Thread.sleep(100);
    } catch (InterruptedException e) {
      log.error("休眠被打断", e);
    }
  }

  @Override
  public void rollback(Iterable<ConsumerRecord<String, String>> records, BaseSettingContext settingContext) {
    Map<TopicPartition, Long> offsets = new HashMap<>();
    for (ConsumerRecord<String, String> record : records) {
      TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
      Long offset = offsets.get(topicPartition);
      if (offset != null) {
        if (record.offset() < offset) {
          offsets.put(topicPartition, record.offset());
        }
      } else {
        offsets.put(topicPartition, record.offset());
      }
    }
    for (Map.Entry<TopicPartition, Long> topicPartitionLongEntry : offsets.entrySet()) {
      kafkaConsumer.seek(topicPartitionLongEntry.getKey(), topicPartitionLongEntry.getValue());
    }
    try {
      //防止单条消息处理占用CPU
      Thread.sleep(100);
    } catch (InterruptedException e) {
      log.error("休眠被打断", e);
    }
  }

  @Override
  public Object rollbackTransactionUnit(ConsumerRecord<String, String> record) {
    return Integer.valueOf(record.partition());
  }

  @Override
  public void close() throws IOException {
    kafkaConsumer.close();
  }
}
