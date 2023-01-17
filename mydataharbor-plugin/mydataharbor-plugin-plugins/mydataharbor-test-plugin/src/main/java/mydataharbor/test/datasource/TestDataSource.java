package mydataharbor.test.datasource;

import mydataharbor.IDataSource;
import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.threadlocal.TaskStorageThreadLocal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;

/**
 * Created by xulang on 2021/8/10.
 */
public class TestDataSource implements IDataSource<Long, BaseSettingContext> {

    private long total;

    private long now = 0L;

    public TestDataSource(long total) {
        this.total = total;
    }

    @Override
    public void init(BaseSettingContext settingContext) {
        Long nowIndex = (Long) TaskStorageThreadLocal.get().getFromCache("now-index");
        if (nowIndex != null) {
            this.now = nowIndex+1;
        }
    }

    @Override
    public String dataSourceType() {
        return "测试数据源";
    }

    @Override
    public Long total() {
        return total;
    }

    @Override
    public Iterable<Long> poll(BaseSettingContext settingContext) throws TheEndException {
        if (now >= total) {
            throw new TheEndException("结束！");
        }
        List<Long> result = new ArrayList<>();
        int count = RandomUtils.nextInt(3, 20);
        while (now < total) {
            if (count >= 0) {
                result.add(now);
                count--;
                now++;
                TaskStorageThreadLocal.get().atomicIncreaseToCache("pull-total", 1L);
            }
            else {
                break;
            }
        }
        TaskStorageThreadLocal.get().setToCache("pull-now-time", System.currentTimeMillis(), System.currentTimeMillis());
        return result;
    }

    @Override
    public void commit(Long record, BaseSettingContext settingContext) {
        TaskStorageThreadLocal.get().atomicIncreaseToCache("total", 1L);
        TaskStorageThreadLocal.get().setToCache("now-index", System.currentTimeMillis(), record);
    }

    @Override
    public void commit(Iterable<Long> records, BaseSettingContext settingContext) {
        for (Long record : records) {
            TaskStorageThreadLocal.get().atomicIncreaseToCache("total", 1L);
            TaskStorageThreadLocal.get().setToCache("now-index", System.currentTimeMillis(), record);
        }

    }

    @Override
    public void rollback(Long record, BaseSettingContext settingContext) {

    }

    @Override
    public void rollback(Iterable<Long> records, BaseSettingContext settingContext) {

    }

    @Override
    public void close() throws IOException {

    }
}
