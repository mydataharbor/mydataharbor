package mydataharbor.source.jdbc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.datasource.AbstractRateLimitDataSource;
import mydataharbor.datasource.RateLimitConfig;
import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.source.jdbc.exception.DataSourceCreateException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * jdbc 数据源
 * Created by xulang on 2021/8/18.
 */
@Slf4j
public abstract class JdbcDataSource extends AbstractRateLimitDataSource<JdbcResult, BaseSettingContext> {

  private BasicDataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  private JdbcDataSourceConfig jdbcDataSourceConfig;

  private SqlRowSet preSqlRowSet;

  /**
   * 全量拉取是否已经ok
   */
  private boolean completePollOk;

  /**
   * 当前结果集是否到头
   */
  private boolean nowRowSetEmpty;

  /**
   * 增量sql
   */
  private String increaseSql;


  private List<JdbcResult> result = new ArrayList<>();

  private volatile boolean commit = true;

  public JdbcDataSource(JdbcDataSourceConfig jdbcDataSourceConfig) {
    super(jdbcDataSourceConfig);
    Properties connectionProps = new Properties();
    connectionProps.put("username", jdbcDataSourceConfig.userName);
    connectionProps.put("password", jdbcDataSourceConfig.password);
    connectionProps.put("driverClassName", driverClassName());
    connectionProps.put("url", jdbcDataSourceConfig.url);
    try {
      dataSource = BasicDataSourceFactory
        .createDataSource(connectionProps);
    } catch (Exception e) {
      throw new DataSourceCreateException("创建jdbc数据源失败！:" + e.getMessage(), e);
    }
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.jdbcDataSourceConfig = jdbcDataSourceConfig;
    fillIncreaseSql(jdbcDataSourceConfig);
  }

  /**
   * 设置增量sql
   *
   * @param jdbcDataSourceConfig
   */
  private void fillIncreaseSql(JdbcDataSourceConfig jdbcDataSourceConfig) {
    switch (jdbcDataSourceConfig.model) {
      case INCREMENT:
      case INCREMENT_AFTER_COMPLETE:
        increaseSql = jdbcDataSourceConfig.url;
        StringBuilder sb = new StringBuilder(increaseSql);
        int index = increaseSql.indexOf(whereFlag);
        if (index >= 0) {
          increaseSql = sb.insert(index + whereFlag.length(), jdbcDataSourceConfig.rollingFieldName + " > ?" + " and ").toString();
        } else {
          int fromIndex = sb.indexOf("from");
          //找到表名后面的位置
          boolean noBlankFlag = false;
          for (int i = fromIndex + 2; i < increaseSql.length(); i++) {
            if (sb.charAt(i) != ' ') {
              noBlankFlag = true;
            }
            if (noBlankFlag && sb.charAt(i) == ' ') {
              sb.insert(i, "where " + jdbcDataSourceConfig.rollingFieldName + " > ? ").toString();
              break;
            }
          }
        }
        break;
    }
  }

  /**
   * 数据库驱动名称
   *
   * @return
   */
  public abstract String driverClassName();

  private String whereFlag = "where ";

  @Override
  public Collection<JdbcResult> doPoll(BaseSettingContext settingContext) throws TheEndException {
    if (!commit) {
      return result;
    }
    SqlRowSet rowSet = null;
    switch (jdbcDataSourceConfig.model) {
      case COMPLETE:
        if (preSqlRowSet == null) {
          preSqlRowSet = jdbcTemplate.queryForRowSet(jdbcDataSourceConfig.url);
        }
        rowSet = preSqlRowSet;
        break;
      case INCREMENT:
        if (preSqlRowSet == null || nowRowSetEmpty) {
          preSqlRowSet = jdbcTemplate.queryForRowSet(increaseSql, getTimeFlag(jdbcDataSourceConfig.rollingFieldAccuracy));
        }
        rowSet = preSqlRowSet;
        break;
      case INCREMENT_AFTER_COMPLETE:
        if (!completePollOk) {
          if (preSqlRowSet == null) {
            preSqlRowSet = jdbcTemplate.queryForRowSet(jdbcDataSourceConfig.url);
          }
          rowSet = preSqlRowSet;
        } else {
          //增量
          if (preSqlRowSet == null || nowRowSetEmpty) {
            preSqlRowSet = jdbcTemplate.queryForRowSet(increaseSql, getTimeFlag(jdbcDataSourceConfig.rollingFieldAccuracy));
          }
          rowSet = preSqlRowSet;
        }
        break;
    }
    result = getResults(rowSet);
    commit = true;
    return result;
  }

  private long getTimeFlag(RollingFieldAccuracy rollingFieldAccuracy) {
    switch (rollingFieldAccuracy) {
      case SECOND:
        return System.currentTimeMillis() / 1000;
      case MILLI_SECOND:
        return System.currentTimeMillis();
    }
    return System.currentTimeMillis();
  }

  private List<JdbcResult> getResults(SqlRowSet resultSet) {
    List<JdbcResult> result = new ArrayList<>();
    int count = 0;
    SqlRowSetMetaData metaData = resultSet.getMetaData();
    while (resultSet.next()) {
      if (count < jdbcDataSourceConfig.maxPollRecords) {
        JdbcResult row = new JdbcResult();
        row.setPosition(resultSet.getRow());
        Map<String, Object> data = new HashMap<>();
        String[] columnNames = metaData.getColumnNames();
        for (String columnName : columnNames) {
          data.put(columnName, resultSet.getObject(columnName));
        }
        row.setData(data);
        result.add(row);
        count++;
      }
    }
    boolean last = resultSet.isLast();
    if (last) {
      if (!completePollOk) {
        completePollOk = true;
      }
      nowRowSetEmpty = true;
    }
    return result;
  }

  @Override
  public Long total() {
    if (jdbcDataSourceConfig.model.equals(JdbcSyncModel.COMPLETE) && jdbcDataSourceConfig.countSql != null && jdbcDataSourceConfig.countSql.length() > 0) {
      Long total = jdbcTemplate.queryForObject(jdbcDataSourceConfig.countSql, Long.class);
      return total;
    }
    return super.total();
  }

  @Override
  public void commit(JdbcResult record, BaseSettingContext settingContext) {

  }

  @Override
  public void commit(Iterable<JdbcResult> records, BaseSettingContext settingContext) {

  }

  @Override
  public void rollback(JdbcResult record, BaseSettingContext settingContext) {
    commit = false;
  }

  @Override
  public void rollback(Iterable<JdbcResult> records, BaseSettingContext settingContext) {
    commit = false;
  }

  @Override
  public String dataSourceType() {
    return "jdbc";
  }


  @Override
  public void close() throws IOException {
    if (dataSource != null) {
      try {
        dataSource.close();
      } catch (SQLException throwables) {
        log.error("关闭数据源失败！", throwables);
      }
    }
  }

  @Data
  public static class JdbcDataSourceConfig extends RateLimitConfig {

    @MyDataHarborMarker(title = "jdbc连接url")
    private String url;

    @MyDataHarborMarker(title = "用户名")
    private String userName;

    @MyDataHarborMarker(title = "密码")
    private String password;

    @MyDataHarborMarker(title = "获取数据的sql语句")
    private String sql;

    @MyDataHarborMarker(title = "单次poll最大返回数量", defaultValue = "200")
    private Integer maxPollRecords;

    @MyDataHarborMarker(title = "模式，增量/全量/先全量后增量", defaultValue = "INCREMENT_AFTER_COMPLETE", des = "默认先全量，后增量")
    private JdbcSyncModel model = JdbcSyncModel.INCREMENT_AFTER_COMPLETE;

    @MyDataHarborMarker(title = "统计sql", require = false, des = "全量模式下用户可以提供统计sql，用于系统计算迁移进度，非必须")
    private String countSql;

    @MyDataHarborMarker(title = "时间滚动字段", require = false, des = "当处于增量模式下，或者处于先全量后增量模式下，该字段必须提供。该字段需要可以表示该条记录的更新时间，时间类型。建议该字段添加索引！")
    private String rollingFieldName;

    @MyDataHarborMarker(title = "滚动字段精度", require = false, defaultValue = "MILLI_SECOND", des = "增量模式下必填")
    private RollingFieldAccuracy rollingFieldAccuracy;

  }

  /**
   * 同步模式
   */
  public static enum JdbcSyncModel {
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

  /**
   * 时间精度
   */
  public static enum RollingFieldAccuracy {
    /**
     * 秒级
     */
    SECOND,

    /**
     * 毫秒
     */
    MILLI_SECOND
  }
}


