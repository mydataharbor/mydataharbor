package mydataharbor.sink.jdbc.config;

public enum WriteModel {
    /**
     * 插入
     */
    INSERT,

    /**
     * 更新
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE,

    /**
     * 先更新，不存在新增
     */
    UPSET
  }