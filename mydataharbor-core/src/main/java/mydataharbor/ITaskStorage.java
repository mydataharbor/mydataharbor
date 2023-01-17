package mydataharbor;

import java.io.Closeable;
import java.io.Serializable;

/**
 * 任务维度
 * 数据持久存储接口
 * 该存储只适合存储一些很必要的持久化数据（如任务因为某种原因重启时读取上次运行时候的数据）
 * 而且不适合经常性的调用，不适合高速存储
 * 插件应尽量设计为无状态的，不过有些无法避免（如jdbc抽取数据）可以使用该接口进行持久存储
 * @author xulang
 * @date 2022/11/18
 */
public interface ITaskStorage extends Runnable, Closeable {

    /**
     * 原子计数，保证最终一致性
     *
     * @param key   要操作的数据
     * @param value 自增值，可正可负
     */
    void atomicIncreaseToCache(String key, Long value);

    /**
     * 获取原子计数的值
     * 该值返回当前jvm实例中最新的值，该值有可能是合并远程的，也有可能是本地的，是一个不准确的值
     * @return
     */
    Long getAtomicValueFromCache(String key);

    /**
     * 将值设置到缓存里
     * 如果一个任务有两个并行管道，使用此方法设置数据，则最终只会保留最新的值
     * @param key
     *
     * @param value
     */
    void setToCache(String key, Long time, Serializable value);

    /**
     * 从缓存获取值
     * @param key
     * @return
     */
    Serializable getFromCache(String key);

    /**
     * 从远程同步数据，如果用户对数据精确度要求非常高可以谨慎调用该方法进行数据同步刷入，性能非常低（视具体实现而定，zookeeper默认实现是很低的）
     * 否则请不要主动调用该方法，该方法会默认1s自动调用一次向存储异步同步数据
     */
    void refresh();

    Boolean isClose();

}
