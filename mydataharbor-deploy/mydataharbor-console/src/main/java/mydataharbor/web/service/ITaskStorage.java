package mydataharbor.web.service;

import java.util.Map;

/**
 * @author xulang
 * @date 2022/12/5
 */
public interface ITaskStorage {
    /**
     * 依据taskID获取所有存储的数据，管理台使用
     * @return
     */
    Map<String, Object> getStoreData(String taskId);
}
