package mydataharbor.source.jdbc;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.datasource.AbstractRateLimitDataSource;
import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;
import mydataharbor.source.jdbc.config.JdbcSyncModel;
import mydataharbor.source.jdbc.exception.DataSourceCreateException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static mydataharbor.source.jdbc.config.JdbcDataSourceConfig.MILLI_SECOND;
import static mydataharbor.source.jdbc.config.JdbcDataSourceConfig.SECOND;

/**
 * jdbc 数据源
 * 全量中断必须从头开始，因为全量考虑性能没有order by
 * 增量模式下，宕机重启，可以从断开处继续拉数据
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
  private boolean completePollOk = false;

  /**
   * 当前结果集是否到头
   */
  private boolean nowRowSetEmpty;

  private List<JdbcResult> tmp = new CopyOnWriteArrayList<>();

  /**
   * 上一次扫描时间
   */
  private Object lastTime;

  /**
   * 全量过程中的最大更新时间
   */
  private Object completeLastTime;

  private Object rollbackUnit = new Object();

  private SimpleDateFormat dateFormat;

  /**
   * 是否第一次poll
   */
  private volatile boolean isFirstPoll = true;

  public JdbcDataSource(JdbcDataSourceConfig jdbcDataSourceConfig) {
    super(jdbcDataSourceConfig);
    this.jdbcDataSourceConfig = jdbcDataSourceConfig;
    Properties connectionProps = new Properties();
    connectionProps.put("username", jdbcDataSourceConfig.getUsername());
    connectionProps.put("password", jdbcDataSourceConfig.getPassword());
    connectionProps.put("driverClassName", driverClassName());
    connectionProps.put("url", jdbcDataSourceConfig.getUrl());
    connectionProps.put("initialSize", jdbcDataSourceConfig.getInitialSize());
    try {
      dataSource = BasicDataSourceFactory
        .createDataSource(connectionProps);
      dataSource.start();
    } catch (Exception e) {
      throw new DataSourceCreateException("创建jdbc数据源失败！:" + e.getMessage(), e);
    }
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.lastTime = getLastTime();
    this.completePollOk = getCompletePollOk();
    if (jdbcDataSourceConfig.getModel() == JdbcSyncModel.INCREMENT || jdbcDataSourceConfig.getModel() == JdbcSyncModel.INCREMENT_AFTER_COMPLETE) {
      if (!MILLI_SECOND.equals(jdbcDataSourceConfig.getTimeFormat()) && !SECOND.equals(jdbcDataSourceConfig.getTimeFormat()))
        this.dateFormat = new SimpleDateFormat(jdbcDataSourceConfig.getTimeFormat());
    }
  }

  /**
   * 获得增量sql
   */
  private String getIncreaseSql() {
    /**
     * 增量sql
     */
    String increaseSql = "";
    switch (jdbcDataSourceConfig.getModel()) {
      case INCREMENT:
      case INCREMENT_AFTER_COMPLETE:
        increaseSql = jdbcDataSourceConfig.getQuerySql() + " ";
        StringBuilder sb = new StringBuilder(increaseSql);
        int index = increaseSql.indexOf(whereFlag);
        if (index >= 0) {
          if (isFirstPoll) {
            increaseSql = sb.insert(index + whereFlag.length(), jdbcDataSourceConfig.getRollingFieldName() + " >= ? and " + jdbcDataSourceConfig.getRollingFieldName() + " < ?" + " and ").toString();
          } else {
            increaseSql = sb.insert(index + whereFlag.length(), jdbcDataSourceConfig.getRollingFieldName() + " > ? and " + jdbcDataSourceConfig.getRollingFieldName() + " < ?" + " and ").toString();
          }
        } else {
          int fromIndex = sb.indexOf(" from ");
          fromIndex = fromIndex == -1 ? sb.indexOf(" FROM ") : fromIndex;
          //找到表名后面的位置
          boolean noBlankFlag = false;
          for (int i = fromIndex + 6; i < increaseSql.length(); i++) {
            if (sb.charAt(i) != ' ') {
              noBlankFlag = true;
            }
            if (noBlankFlag && sb.charAt(i) == ' ') {
              if (isFirstPoll) {
                increaseSql = sb.insert(i, " where " + jdbcDataSourceConfig.getRollingFieldName() + " >= ? and " + jdbcDataSourceConfig.getRollingFieldName() + " < ? ").toString();
              } else {
                increaseSql = sb.insert(i, " where " + jdbcDataSourceConfig.getRollingFieldName() + " > ? and " + jdbcDataSourceConfig.getRollingFieldName() + " < ? ").toString();
              }
              break;
            }
          }
        }
        break;
    }
    //增加 order by
    increaseSql += " order by " + jdbcDataSourceConfig.getRollingFieldName() + " asc";
    return increaseSql;
  }

  /**
   * 数据库驱动名称
   *
   * @return
   */
  public abstract String driverClassName();

  private String whereFlag = "where ";

  public Object getNowTime() {
    if (dateFormat != null) {
      return dateFormat.format(System.currentTimeMillis());
    } else if (SECOND.equals(jdbcDataSourceConfig.getTimeFormat())) {
      return System.currentTimeMillis() / 1000;
    } else {
      return System.currentTimeMillis();
    }
  }

  @Override
  public Collection<JdbcResult> doPoll(BaseSettingContext settingContext) throws TheEndException {
    if (!tmp.isEmpty()) {
      return tmp;
    }
    SqlRowSet rowSet = null;
    switch (jdbcDataSourceConfig.getModel()) {
      case COMPLETE:
        if (completePollOk) {
          throw new TheEndException("迁移结束");
        }
        if (preSqlRowSet == null) {
          preSqlRowSet = jdbcTemplate.queryForRowSet(jdbcDataSourceConfig.getQuerySql());
        }
        rowSet = preSqlRowSet;
        break;
      case INCREMENT:
        if (preSqlRowSet == null || nowRowSetEmpty) {
          preSqlRowSet = jdbcTemplate.queryForRowSet(getIncreaseSql(), lastTime, getNowTime());
        }
        rowSet = preSqlRowSet;
        break;
      case INCREMENT_AFTER_COMPLETE:
        if (!completePollOk) {
          if (preSqlRowSet == null) {
            preSqlRowSet = jdbcTemplate.queryForRowSet(jdbcDataSourceConfig.getQuerySql());
          }
          rowSet = preSqlRowSet;
        } else {
          //增量
          if (preSqlRowSet == null || nowRowSetEmpty) {
            preSqlRowSet = jdbcTemplate.queryForRowSet(getIncreaseSql(), lastTime, getNowTime());
          }
          rowSet = preSqlRowSet;
        }
        break;
    }
    tmp = getResults(rowSet);
    isFirstPoll = false;
    return tmp;
  }

  public Object getLastTime() {
    //TODO 这里这个时间要先从永久存储取，如果没有使用配置
    return jdbcDataSourceConfig.getStartTime();
  }

  public void setLastTime(Object lastTime) {
    //TODO 把这个值持久化存起来
    this.lastTime = lastTime;
  }

  public void setCompletePollOk(boolean completePollOk) {
    //TODO 把这个值持久化存起来
    this.completePollOk = completePollOk;
    if (completeLastTime != null)
      setLastTime(completeLastTime);
  }

  public boolean getCompletePollOk() {
    //TODO 这里这个时间要先从永久存储取，如果没有返回false
    return false;
  }

  private List<JdbcResult> getResults(SqlRowSet resultSet) {
    List<JdbcResult> result = new CopyOnWriteArrayList<>();
    int count = 0;
    SqlRowSetMetaData metaData = resultSet.getMetaData();
    String[] columnNames = metaData.getColumnNames();
    while (count < jdbcDataSourceConfig.getMaxPollRecords()) {
      if (resultSet.next()) {
        JdbcResult row = new JdbcResult();
        row.setJdbcSyncModel(jdbcDataSourceConfig.getModel());
        row.setPosition(resultSet.getRow());
        Map<String, Object> data = new HashMap<>();
        for (String columnName : columnNames) {
          Object columnValue = resultSet.getObject(columnName);
          if (jdbcDataSourceConfig.getPrimaryKeys() != null && jdbcDataSourceConfig.getPrimaryKeys().contains(columnName)) {
            row.getPrimaryKeysValues().put(columnName, columnValue);
          }
          if (columnName.equals(jdbcDataSourceConfig.getRollingFieldName())) {
            row.setTimeFlag(columnValue);
            if (completePollOk) {
              //增量情况下才更新
              setLastTime(columnValue);
            } else {
              if (completeLastTime == null) {
                completeLastTime = columnValue;
              } else {
                if (completeLastTime instanceof Comparable && columnValue instanceof Comparable) {
                  if (((Comparable) columnValue).compareTo(completeLastTime) > 0) {
                    completeLastTime = columnValue;
                  }
                }
              }
            }
          }
          data.put(columnName, columnValue);
        }
        row.setData(data);
        result.add(row);
        count++;
      } else {
        if (!completePollOk) {
          setCompletePollOk(true);
        }
        nowRowSetEmpty = true;
        break;
      }
    }
    return result;
  }

  @Override
  public Long total() {
    if (jdbcDataSourceConfig.getModel().equals(JdbcSyncModel.COMPLETE) && jdbcDataSourceConfig.getCountSql() != null && jdbcDataSourceConfig.getCountSql().length() > 0) {
      Long total = jdbcTemplate.queryForObject(jdbcDataSourceConfig.getCountSql(), Long.class);
      return total;
    }
    return super.total();
  }

  @Override
  public Object rollbackTransactionUnit(JdbcResult record) {
    //rollback 只有一个回滚单元
    return rollbackUnit;
  }

  @Override
  public void commit(JdbcResult record, BaseSettingContext settingContext) {
    synchronized (tmp) {
      tmp.remove(record);
    }
  }

  @Override
  public void commit(Iterable<JdbcResult> records, BaseSettingContext settingContext) {
    if (records instanceof Collection) {
      synchronized (tmp) {
        tmp.removeAll((Collection<?>) records);
      }
    }
  }

  @Override
  public void rollback(JdbcResult record, BaseSettingContext settingContext) {

  }

  @Override
  public void rollback(Iterable<JdbcResult> records, BaseSettingContext settingContext) {

  }

  @Override
  public String dataSourceType() {
    return "jdbc-";
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


}


