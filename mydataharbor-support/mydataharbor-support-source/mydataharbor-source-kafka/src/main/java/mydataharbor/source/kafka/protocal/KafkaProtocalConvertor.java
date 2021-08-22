package mydataharbor.source.kafka.protocal;

import mydataharbor.IProtocalDataConvertor;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

/**
 * Created by xulang on 2021/8/22.
 */
@MyDataHarborMarker(title = "kafka数据源默认协议数据转换器")
public class KafkaProtocalConvertor implements IProtocalDataConvertor<ConsumerRecord<String, String>, KafkaProtocalData, BaseSettingContext> {

  @Override
  public KafkaProtocalData convert(ConsumerRecord<String, String> record, BaseSettingContext settingContext) throws ResetException {
    KafkaProtocalData kafkaProtocalData = new KafkaProtocalData();
    kafkaProtocalData.setKey(record.key());
    kafkaProtocalData.setValue(record.value());
    kafkaProtocalData.setTopic(record.topic());
    for (Header header : record.headers()) {
      kafkaProtocalData.getHeader().put(header.key(), header.value());
    }
    return kafkaProtocalData;
  }

}
