package mydataharbor.plugin.jdbc.mysql.sink;

import mydataharbor.sink.jdbc.JdbcSink;
import mydataharbor.sink.jdbc.config.JdbcSinkConfig;
import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcMysql50xSink extends JdbcSink {

  public JdbcMysql50xSink(JdbcSinkConfig jdbcSinkConfig) {
    super(jdbcSinkConfig);
  }

  @Override
  public String driverClassName() {
    return "com.mysql.jdbc.Driver";
  }

  @Override
  public String name() {
    return super.name() + "mysql-5.0.x";
  }
}
