package mydataharbor.plugin.jdbc.oracle.source;

import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcOracle1911xDataSource extends JdbcDataSource {

  public JdbcOracle1911xDataSource(JdbcDataSourceConfig jdbcDataSourceConfig) {
    super(jdbcDataSourceConfig);
  }

  @Override
  public String driverClassName() {
    return "oracle.jdbc.driver.OracleDriver";
  }

  @Override
  public String dataSourceType() {
    return super.dataSourceType() + "ojdbc8-21.1.x";
  }
}
