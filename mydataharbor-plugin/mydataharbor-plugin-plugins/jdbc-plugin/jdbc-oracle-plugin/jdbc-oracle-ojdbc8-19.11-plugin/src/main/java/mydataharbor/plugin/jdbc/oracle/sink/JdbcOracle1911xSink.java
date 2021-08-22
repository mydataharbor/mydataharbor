package mydataharbor.plugin.jdbc.oracle.sink;

import mydataharbor.sink.jdbc.JdbcSink;
import mydataharbor.sink.jdbc.config.JdbcSinkConfig;
import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcOracle1911xSink extends JdbcSink {

  public JdbcOracle1911xSink(JdbcSinkConfig jdbcSinkConfig) {
    super(jdbcSinkConfig);
  }

  @Override
  public String driverClassName() {
    return "oracle.jdbc.driver.OracleDriver";
  }

  @Override
  public String name() {
    return super.name() + "ojdbc8-21.1.x";
  }
}
