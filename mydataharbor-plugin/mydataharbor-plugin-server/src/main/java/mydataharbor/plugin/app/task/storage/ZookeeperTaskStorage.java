package mydataharbor.plugin.app.task.storage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.ITaskStorage;
import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.exception.PluginServerCreateException;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.ObjectWrapper;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.rpc.util.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.data.Stat;

/**
 * zookeeper实现的task storage
 * 每5秒和zookeeper进行数据同步
 *
 * @author xulang
 * @date 2022/11/18
 */
@Slf4j
public class ZookeeperTaskStorage implements ITaskStorage {
    /**
     * zk连接对象
     */
    private CuratorFramework client;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 原子计数型数据
     */
    private volatile Map<String, AtomicLong> atomicLongIncreaseMap;
    /**
     * 原子计数型数据
     */
    private volatile Map<String, AtomicLong> atomicLongMap;
    /**
     * 对象型数据
     */
    private volatile Map<String, ObjectWrapper> objectMap;
    /**
     * 关闭
     */
    private volatile Boolean close;

    private ClassLoader classLoader;

    private InterProcessMutex interProcessMutex;

    public ZookeeperTaskStorage(String taskId, CuratorFramework client, ClassLoader classLoader) {
        this.taskId = taskId;
        this.client = client;
        this.classLoader = classLoader;
        this.close = false;
        this.atomicLongIncreaseMap = new ConcurrentHashMap<>();
        this.atomicLongMap = new ConcurrentHashMap<>();
        this.objectMap = new ConcurrentHashMap<>();
        this.interProcessMutex = new InterProcessMutex(client, Constant.TASK_DATA_STORAGE_LOCK_PATH + taskId);
        refresh();
        //监听task状态变更，如果task结束那么结束该存储器的同步线程
        NodeCache nodeCache = new NodeCache(client, Constant.TASK_PATH_PARENT + taskId);
        try {
            nodeCache.getListenable().addListener(() -> {
                if (nodeCache.getCurrentData() == null || JsonUtil.deserialize(nodeCache.getCurrentData().getData(), DistributedTask.class).getTaskState().equals(TaskState.over)) {
                    new Thread() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            Thread.sleep(10000);//10s后再关闭，防止数据没有完全同步过去
                            refresh();
                            close();
                            nodeCache.close();
                        }
                    }.start();
                }
            });
            nodeCache.start();
        } catch (Exception e) {
            throw new PluginServerCreateException("启动task状态变更监听异常！", e);
        }
        new Thread(this).start();
    }

    @Override
    public void atomicIncreaseToCache(String key, Long value) {
        assertNotClose();
        AtomicLong atomicLong = atomicLongIncreaseMap.get(key);
        if (atomicLong == null) {
            synchronized (this) {
                atomicLong = atomicLongIncreaseMap.get(key);
                if (atomicLong == null) {
                    atomicLongMap.put(key, new AtomicLong(value));
                    atomicLongIncreaseMap.put(key, new AtomicLong(value));
                }
            }
        }
        else {
            atomicLongIncreaseMap.get(key).addAndGet(value);
            atomicLongMap.get(key).addAndGet(value);
        }
    }

    @Override
    public Long getAtomicValueFromCache(String key) {
        assertNotClose();
        AtomicLong atomicLong = atomicLongMap.get(key);
        if (atomicLong != null)
            return atomicLong.get();
        return null;
    }

    @Override
    public void setToCache(String key, Long time, Serializable value) {
        assertNotClose();
        synchronized (this) {
            ObjectWrapper objectWrapper = objectMap.get(key);
            if (objectWrapper == null) {
                objectWrapper = new ObjectWrapper(time, value);
                objectMap.put(key, objectWrapper);
            }
            else {
                if (time > objectWrapper.getTime()) {
                    objectWrapper.setTime(time);
                    objectWrapper.setObj(value);
                }
            }
        }
    }

    @Override
    public Serializable getFromCache(String key) {
        assertNotClose();
        ObjectWrapper objectWrapper = objectMap.get(key);
        return objectWrapper == null ? null : objectWrapper.getObj();
    }

    @Override
    public void refresh() {
        synchronized (this) {
            try {
                interProcessMutex.acquire(5, TimeUnit.SECONDS);
                //先输出，然后在更新本地
                String atomicLongMapPath = Constant.TASK_DATA_STORAGE_PATH + taskId + "/atomicLongMap";
                Stat stat = client.checkExists().forPath(atomicLongMapPath);
                byte[] atomicLongMapBytes = null;
                if (stat != null)
                    atomicLongMapBytes = client.getData().forPath(atomicLongMapPath);
                if (atomicLongMapBytes != null) {
                    Map<String, AtomicLong> atomicLongMapZk = (Map<String, AtomicLong>) new ObjectInputStream(new ByteArrayInputStream(atomicLongMapBytes)).readObject();
                    HashSet<String> keys = new HashSet<>(atomicLongIncreaseMap.keySet());
                    for (String key : keys) {
                        long value = atomicLongIncreaseMap.get(key).get();
                        if (atomicLongMapZk.containsKey(key))
                            atomicLongMapZk.get(key).addAndGet(value);
                        else
                            atomicLongMapZk.put(key, new AtomicLong(value));
                        atomicLongIncreaseMap.get(key).addAndGet(-value);
                    }
                    this.atomicLongMap = atomicLongMapZk;
                }
                //更新
                ByteArrayOutputStream atomicLongMapByteArrayOutputStream = new ByteArrayOutputStream();
                new ObjectOutputStream(atomicLongMapByteArrayOutputStream).writeObject(this.atomicLongMap);
                if (client.checkExists().forPath(atomicLongMapPath) != null)
                    client.setData().forPath(atomicLongMapPath, atomicLongMapByteArrayOutputStream.toByteArray());
                else
                    client.create().creatingParentContainersIfNeeded().forPath(atomicLongMapPath, atomicLongMapByteArrayOutputStream.toByteArray());

                String objectMapPath = Constant.TASK_DATA_STORAGE_PATH + taskId + "/objectMap";
                String objectMapJsonPath = Constant.TASK_DATA_STORAGE_PATH + taskId + "/objectMapJson";
                Stat stat1 = client.checkExists().forPath(objectMapPath);
                byte[] objectMapBytes = null;
                if (stat1 != null) {
                    objectMapBytes = client.getData().forPath(objectMapPath);
                }
                if (objectMapBytes != null) {
                    Map<String, ObjectWrapper> objectMapZk = (Map<String, ObjectWrapper>) new ClassLoaderObjectInputStream(classLoader, new ByteArrayInputStream(objectMapBytes)).readObject();
                    HashSet<String> keys = new HashSet<>(objectMap.keySet());
                    for (String key : keys) {
                        ObjectWrapper localObjectWrapper = objectMap.get(key);
                        if (!objectMapZk.containsKey(key)) {
                            objectMapZk.put(key, localObjectWrapper);
                        }
                        else {
                            ObjectWrapper zkObjectWrapper = objectMapZk.get(key);
                            if (localObjectWrapper.getTime() > zkObjectWrapper.getTime()) {
                                objectMapZk.put(key, localObjectWrapper);
                            }
                            else {
                                objectMap.put(key, zkObjectWrapper);
                            }
                        }
                    }
                    for (String key : objectMapZk.keySet()) {
                        ObjectWrapper zkObjectWrapper = objectMapZk.get(key);
                        if (!objectMap.containsKey(key)) {
                            objectMap.put(key, zkObjectWrapper);
                        }
                    }
                }
                //更新
                ByteArrayOutputStream objectMapByteArrayOutputStream = new ByteArrayOutputStream();
                new ObjectOutputStream(objectMapByteArrayOutputStream).writeObject(this.objectMap);
                if (client.checkExists().forPath(objectMapPath) != null) {
                    client.setData().forPath(objectMapPath, objectMapByteArrayOutputStream.toByteArray());
                    client.setData().forPath(objectMapJsonPath, JsonUtil.serialize(objectMap));
                }
                else {
                    client.create().creatingParentContainersIfNeeded().forPath(objectMapPath, objectMapByteArrayOutputStream.toByteArray());
                    client.create().creatingParentContainersIfNeeded().forPath(objectMapJsonPath, JsonUtil.serialize(objectMap));
                }
            }
            catch (Exception e) {
                log.error("刷新任务存储数据时发生异常", e);
            }
            finally {
                try {
                    interProcessMutex.release();
                }
                catch (Exception e) {
                    log.error("释放任务存储数据锁失败");
                }
            }
        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                log.error("睡眠被打断");
            }
            try {
                refresh();
            }
            catch (Exception e) {
                log.error("自动同步刷新任务失败");
            }
            if (close) {
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.close = true;
    }

    private void assertNotClose() {
        if (close) {
            throw new IllegalStateException("该任务的存储已经被关闭，禁止写入读取");
        }
    }

    public Boolean isClose() {
        return close;
    }
}
