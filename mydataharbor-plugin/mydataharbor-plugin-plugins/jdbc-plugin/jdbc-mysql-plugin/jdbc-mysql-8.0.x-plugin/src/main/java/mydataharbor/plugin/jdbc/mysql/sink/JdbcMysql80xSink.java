package mydataharbor.plugin.jdbc.mysql.sink;

import mydataharbor.sink.jdbc.JdbcSink;
import mydataharbor.sink.jdbc.config.JdbcSinkConfig;
import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcMysql80xSink extends JdbcSink {

  public JdbcMysql80xSink(JdbcSinkConfig jdbcSinkConfig) {
    super(jdbcSinkConfig);
  }

  @Override
  public String driverClassName() {
    return "com.mysql.cj.jdbc.Driver";
  }

  @Override
  public String name() {
    return super.name() + "mysql-8.0.x";
  }
}
