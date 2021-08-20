package mydataharbor.plugin.source.jdbc.oracle;

import mydataharbor.source.jdbc.JdbcDataSource;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcMysql51xDataSource extends JdbcDataSource {

  public JdbcMysql51xDataSource(JdbcDataSourceConfig jdbcDataSourceConfig) {
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
