package mydataharbor.test.sink;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.IDataSink;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.threadlocal.TaskStorageThreadLocal;

import java.io.IOException;
import java.util.List;

/**
 * Created by xulang on 2021/8/10.
 */
@Slf4j
public class TestSink implements IDataSink<Long, BaseSettingContext> {
    @Override
    public String name() {
        return "测试写入器";
    }

    @Override
    public WriterResult write(Long record, BaseSettingContext settingContext) throws ResetException {
        log.info("测试写入器单条写入:{}", record);
        TaskStorageThreadLocal.get().atomicIncreaseToCache("write-total", 1L);
        TaskStorageThreadLocal.get().setToCache("write-last", System.currentTimeMillis(), record);
        return WriterResult.builder().commit(true).success(true).msg("ok").build();
    }

    @Override
    public WriterResult write(List<Long> records, BaseSettingContext settingContext) throws ResetException {
        log.info("测试写入器批量写入:{}", records);
        TaskStorageThreadLocal.get().atomicIncreaseToCache("write-total", (long) records.size());
        TaskStorageThreadLocal.get().setToCache("write-last", System.currentTimeMillis(), records.get(records.size()-1));
        return WriterResult.builder().commit(true).success(true).msg("ok").build();
    }

    @Override
    public void close() throws IOException {

    }
}
