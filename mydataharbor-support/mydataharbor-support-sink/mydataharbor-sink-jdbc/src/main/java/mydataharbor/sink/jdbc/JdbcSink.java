package mydataharbor.sink.jdbc;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.IDataSink;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.jdbc.config.JdbcSinkConfig;
import mydataharbor.sink.jdbc.exception.DataSourceCreateException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * jdbc 写入源
 * Created by xulang on 2021/8/18.
 */
@Slf4j
public abstract class JdbcSink implements IDataSink<JdbcSinkReq, BaseSettingContext> {

  private BasicDataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  private JdbcSinkConfig jdbcSinkConfig;

  private DataSourceTransactionManager dataSourceTransactionManager;

  public JdbcSink(JdbcSinkConfig jdbcSinkConfig) {
    this.jdbcSinkConfig = jdbcSinkConfig;
    Properties connectionProps = new Properties();
    connectionProps.put("username", jdbcSinkConfig.getUsername());
    connectionProps.put("password", jdbcSinkConfig.getPassword());
    connectionProps.put("driverClassName", driverClassName());
    connectionProps.put("url", jdbcSinkConfig.getUrl());
    connectionProps.put("initialSize", jdbcSinkConfig.getInitialSize());
    try {
      dataSource = BasicDataSourceFactory
        .createDataSource(connectionProps);
      dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
      dataSource.start();
      jdbcTemplate = new JdbcTemplate(dataSource);
    } catch (Exception e) {
      throw new DataSourceCreateException("创建jdbc数据源失败！:" + e.getMessage(), e);
    }
  }

  /**
   * 数据库驱动名称
   *
   * @return
   */
  public abstract String driverClassName();


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


  @Override
  public String name() {
    return "jdbc";
  }


  public static <T> T[] concat(T[] first, T[] second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  @Override
  public WriterResult write(JdbcSinkReq record, BaseSettingContext settingContext) throws ResetException {
    TransactionDefinition definition = new DefaultTransactionDefinition();
    TransactionStatus status = dataSourceTransactionManager.getTransaction(definition);
    List<Integer> result;
    try {
      result = process(record);
      dataSourceTransactionManager.commit(status);
    } catch (Exception e) {
      log.error("写入数据时发生异常", e);
      dataSourceTransactionManager.rollback(status);
      throw new ResetException("写入数据时发生异常：" + e.getMessage());
    }
    return WriterResult.builder().success(true).commit(true).msg("写入成功").writeReturn(result).build();
  }

  private List<Integer> process(JdbcSinkReq record) throws Exception {
    List<Integer> result = new ArrayList<>();
    try {
      for (JdbcSinkReq.WriteDataInfo writeDataInfo : record.getWriteDataInfos()) {
        Map<String, Object> data = writeDataInfo.getData();
        Set<Map.Entry<String, Object>> dataEntrySet = data.entrySet();
        String insertColumnNames = dataEntrySet.stream().map(o -> o.getKey()).collect(Collectors.joining(", ", "(", ")"));
        String updateColumnNames = dataEntrySet.stream().map(o -> o.getKey() + " = ?").collect(Collectors.joining(", "));
        String valuePlaceholder = dataEntrySet.stream().map(o -> "?").collect(Collectors.joining(", ", "(", ")"));
        Object[] values = dataEntrySet.stream().map(o -> o.getValue()).toArray();
        StringBuilder whereSql = new StringBuilder();
        Object[] whereValues = new Object[0];
        if (writeDataInfo.getWhere() != null) {
          whereSql = new StringBuilder();
          whereSql.append(" WHERE ");
          whereSql.append(writeDataInfo.getWhere().keySet().stream().map(o -> o + " = ?").collect(Collectors.joining("and ")));
          whereValues = writeDataInfo.getWhere().values().stream().toArray();
        }
        StringBuilder sql = new StringBuilder();
        switch (writeDataInfo.getWriteModel()) {
          case INSERT:
            sql.append("INSERT INTO ");
            sql.append(writeDataInfo.getTableName());
            sql.append(" ");
            sql.append(insertColumnNames);
            sql.append(" VALUES ");
            sql.append(valuePlaceholder);
            int insertUpdate = jdbcTemplate.update(sql.toString(), values);
            result.add(insertUpdate);
            break;
          case UPDATE:
            sql.append("UPDATE ");
            sql.append(writeDataInfo.getTableName());
            sql.append(" SET ");
            sql.append(updateColumnNames);
            if (whereValues.length > 0) {
              sql.append(whereSql.toString());
            }
            int update = jdbcTemplate.update(sql.toString(), concat(values, whereValues));
            result.add(update);
            break;
          case DELETE:
            sql.append("DELETE FROM ");
            sql.append(writeDataInfo.getTableName());
            if (whereValues.length > 0) {
              sql.append(whereSql.toString());
            }
            int deleteUpdate = jdbcTemplate.update(sql.toString(), whereValues);
            result.add(deleteUpdate);
            break;
          case UPSET:
            sql.append("UPDATE ");
            sql.append(writeDataInfo.getTableName());
            sql.append(" SET ");
            sql.append(updateColumnNames);
            if (whereValues.length > 0) {
              sql.append(whereSql.toString());
            }
            int upset = jdbcTemplate.update(sql.toString(), concat(values, whereValues));
            if (upset == 0) {
              sql = new StringBuilder();
              sql.append("INSERT INTO ");
              sql.append(writeDataInfo.getTableName());
              sql.append(" ");
              sql.append(insertColumnNames);
              sql.append(" VALUES ");
              sql.append(valuePlaceholder);
              upset = jdbcTemplate.update(sql.toString(), values);
            }
            result.add(upset);
            break;
        }
      }
    } catch (Exception e) {
      log.error("写入数据时发生异常", e);
      throw e;
    }
    return result;
  }

  @Override
  public WriterResult write(List<JdbcSinkReq> records, BaseSettingContext settingContext) throws ResetException {
    List<List<Integer>> result = new ArrayList<>();
    for (JdbcSinkReq record : records) {
      TransactionDefinition definition = new DefaultTransactionDefinition();
      TransactionStatus status = dataSourceTransactionManager.getTransaction(definition);
      try {
        List<Integer> recodResult = process(record);
        result.add(recodResult);
        dataSourceTransactionManager.commit(status);
      } catch (Exception e) {
        log.error("写入数据时发生异常", e);
        dataSourceTransactionManager.rollback(status);
        throw new ResetException("写入数据时发生异常：" + e.getMessage());
      }
    }
    return WriterResult.builder().success(true).commit(true).msg("ok").writeReturn(result).build();
  }
}


