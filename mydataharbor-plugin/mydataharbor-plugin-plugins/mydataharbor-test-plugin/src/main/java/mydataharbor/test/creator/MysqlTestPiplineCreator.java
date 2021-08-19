package mydataharbor.test.creator;

import lombok.Data;
import mydataharbor.IDataPipline;
import mydataharbor.IDataSinkCreator;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.convertor.data.OriginalDataConvertor;
import mydataharbor.convertor.protocal.OriginalProtocalDataConvertor;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.sink.jdbc.mysql.JdbcMysql51xDataSource;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.source.jdbc.JdbcDataSource;
import mydataharbor.test.datasource.TestDataSource;
import mydataharbor.test.sink.TestSink;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * Created by xulang on 2021/8/10.
 */
@Extension
public class MysqlTestPiplineCreator implements IDataSinkCreator<MysqlTestPiplineCreator.MysqlTestPiplineCretorConfig, BaseSettingContext>, ExtensionPoint {


  @Override
  public String type() {
    return "mysql test管道创建器";
  }

  @Override
  public IDataPipline createPipline(MysqlTestPiplineCretorConfig config, BaseSettingContext settingContext) throws Exception {
    CommonDataPipline commonDataPipline = CommonDataPipline.builder()
      .dataSource(new JdbcMysql51xDataSource(config.jdbcDataSourceConfig))
      .protocalDataConvertor(new OriginalProtocalDataConvertor())
      .dataConvertor(new OriginalDataConvertor())
      .sink(new TestSink())
      .settingContext(settingContext)
      .build();
    return commonDataPipline;
  }

  @Override
  public <T> T parseJson(String json, Class<T> clazz) {
    return JsonUtil.jsonToObject(json, clazz);
  }

  @Data
  public static class MysqlTestPiplineCretorConfig {

    @MyDataHarborMarker(title = "数据库连接信息")
    private JdbcDataSource.JdbcDataSourceConfig jdbcDataSourceConfig;

  }

}

