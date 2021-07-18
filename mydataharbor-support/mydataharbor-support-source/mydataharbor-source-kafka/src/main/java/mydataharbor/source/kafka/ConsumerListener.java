package mydataharbor.source.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;

/**
 * @auth xulang
 * @Date 2021/4/30
 **/
public class ConsumerListener implements ConsumerRebalanceListener {

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

  }
}
