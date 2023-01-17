package mydataharbor.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.task.ObjectWrapper;
import mydataharbor.rpc.util.JsonUtil;
import mydataharbor.web.service.INodeService;
import mydataharbor.web.service.ITaskStorage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xulang
 * @date 2022/12/5
 */
@Slf4j
@Component
public class ZookeeperTaskStorage implements ITaskStorage {

    @Autowired
    private INodeService nodeService;

    @Override
    public Map<String, Object> getStoreData(String taskId) {
        Map<String, Object> result = new HashMap<>();
        String atomicLongMapPath = Constant.TASK_DATA_STORAGE_PATH + taskId + "/atomicLongMap";
        try {
            byte[] data = nodeService.getClient().getData().forPath(atomicLongMapPath);
            Map<String, AtomicLong> atomicLongMapZk = (Map<String, AtomicLong>) new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
            result.putAll(atomicLongMapZk);
        }
        catch (Exception e) {
            log.error("从zookeeper获取自增类型存储数据失败", e);
        }
        String objectMapJsonPath = Constant.TASK_DATA_STORAGE_PATH + taskId + "/objectMapJson";
        try {
            byte[] data = nodeService.getClient().getData().forPath(objectMapJsonPath);
            Map<String, ObjectWrapper> objectMap = JsonUtil.jsonToObjectHashMap(new String(data), String.class, ObjectWrapper.class);
            for (Map.Entry<String, ObjectWrapper> objectWrapperEntry : objectMap.entrySet()) {
                result.put(objectWrapperEntry.getKey(),objectWrapperEntry.getValue().getObj());
            }
        }
        catch (Exception e) {
            log.error("从zookeeper获取对象类型存储数据失败", e);
        }
        return result;
    }
}
