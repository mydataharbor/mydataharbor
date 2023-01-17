package mydataharbor.threadlocal;

import mydataharbor.ITaskStorage;

/**
 * @author xulang
 * @date 2022/11/18
 */
public class TaskStorageThreadLocal {
    private static ThreadLocal<ITaskStorage> taskStorageThreadLocal = new ThreadLocal<>();

    public static void set(ITaskStorage taskStorage){
        taskStorageThreadLocal.set(taskStorage);
    }

    public static ITaskStorage get(){
        return taskStorageThreadLocal.get();
    }
}
