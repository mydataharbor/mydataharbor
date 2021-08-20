package mydataharbor.test.sink;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.IDataSink;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

import java.io.IOException;
import java.util.List;

/**
 * Created by xulang on 2021/8/10.
 */
@Slf4j
public class ObjectSink implements IDataSink<Object, BaseSettingContext> {
  @Override
  public String name() {
    return "测试写入器";
  }

  @Override
  public WriterResult write(Object record, BaseSettingContext settingContext) throws ResetException {
    log.info("测试写入器单条写入:{}", record);
    return WriterResult.builder().commit(true).success(true).msg("ok").build();
  }

  @Override
  public WriterResult write(List<Object> records, BaseSettingContext settingContext) throws ResetException {
    log.info("测试写入器批量写入:{}", records);
    return WriterResult.builder().commit(true).success(true).msg("ok").build();
  }

  @Override
  public void close() throws IOException {

  }
}
