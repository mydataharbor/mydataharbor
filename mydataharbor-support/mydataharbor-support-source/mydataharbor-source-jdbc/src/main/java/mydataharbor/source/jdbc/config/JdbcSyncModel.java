package mydataharbor.source.jdbc.config;

/**
   * 同步模式
   */
  public  enum JdbcSyncModel {
    /**
     * 增量
     */
    INCREMENT,

    /**
     * 全量
     */
    COMPLETE,

    /**
     * 先全量后增量
     */
    INCREMENT_AFTER_COMPLETE

  }