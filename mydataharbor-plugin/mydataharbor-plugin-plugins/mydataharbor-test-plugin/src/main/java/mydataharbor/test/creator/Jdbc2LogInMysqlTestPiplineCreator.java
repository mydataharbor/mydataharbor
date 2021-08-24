/*
package mydataharbor.test.creator;

import mydataharbor.sink.jdbc.config.JdbcSinkConfig;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import lombok.Data;
import mydataharbor.IDataPipline;
import mydataharbor.IDataSinkCreator;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.convertor.data.OriginalDataConvertor;
import mydataharbor.convertor.protocal.OriginalProtocalDataConvertor;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.jdbc.mysql.source.JdbcMysql51xDataSource;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;
import mydataharbor.test.sink.ObjectSink;

*/
/**
 * Created by xulang on 2021/8/10.
 *//*

@Extension
public class Jdbc2LogInMysqlTestPiplineCreator implements IDataSinkCreator<Jdbc2LogInMysqlTestPiplineCreator.MysqlTestPiplineCretorConfig, BaseSettingContext>, ExtensionPoint {


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
      .sink(new ObjectSink())
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

    @MyDataHarborMarker(title = "数据库数据源信息")
    private JdbcDataSourceConfig jdbcDataSourceConfig;

    @MyDataHarborMarker(title = "数据库写入源信息")
    private JdbcSinkConfig jdbcSinkConfig;

  }

}

*/
