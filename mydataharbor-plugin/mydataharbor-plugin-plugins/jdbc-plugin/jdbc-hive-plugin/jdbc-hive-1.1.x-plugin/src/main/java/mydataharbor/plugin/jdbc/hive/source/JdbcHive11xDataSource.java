package mydataharbor.plugin.jdbc.hive.source;

import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcHive11xDataSource extends JdbcDataSource {

  public JdbcHive11xDataSource(JdbcDataSourceConfig jdbcDataSourceConfig) {
    super(jdbcDataSourceConfig);
  }

  @Override
  public String driverClassName() {
    return "org.apache.hive.jdbc.HiveDriver";
  }

  @Override
  public String dataSourceType() {
    return super.dataSourceType() + "hive-1.1.x";
  }
}
