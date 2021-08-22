package mydataharbor.plugin.jdbc.hive.source;

import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;

/**
 * Created by xulang on 2021/8/19.
 */
public class JdbcHive10xDataSource extends JdbcDataSource {

  public JdbcHive10xDataSource(JdbcDataSourceConfig jdbcDataSourceConfig) {
    super(jdbcDataSourceConfig);
  }

  @Override
  public String driverClassName() {
    return "org.apache.hive.jdbc.HiveDriver";
  }

  @Override
  public String dataSourceType() {
    return super.dataSourceType() + "hive-1.0.x";
  }
}
