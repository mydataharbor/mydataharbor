package mydataharbor.test.creator;

import lombok.Data;
import mydataharbor.IDataConvertor;
import mydataharbor.IDataPipline;
import mydataharbor.IDataSinkCreator;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.jdbc.mysql.sink.JdbcMysql51xSink;
import mydataharbor.plugin.jdbc.mysql.source.JdbcMysql51xDataSource;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.jdbc.JdbcSinkReq;
import mydataharbor.sink.jdbc.config.JdbcSinkConfig;
import mydataharbor.source.jdbc.config.JdbcDataSourceConfig;
import mydataharbor.source.jdbc.protocal.JdbcProtocalConvertor;
import mydataharbor.source.jdbc.protocal.JdbcProtocalData;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xulang on 2021/8/10.
 */
@Extension
public class Jdbc2JdbcInMysqlTestPiplineCreator implements IDataSinkCreator<Jdbc2JdbcInMysqlTestPiplineCreator.MysqlTestPiplineCretorConfig, BaseSettingContext>, ExtensionPoint {


  @Override
  public String type() {
    return "mysql test管道创建器";
  }

  @Override
  public IDataPipline createPipline(MysqlTestPiplineCretorConfig config, BaseSettingContext settingContext) throws Exception {
    CommonDataPipline commonDataPipline = CommonDataPipline.builder()
      .dataSource(new JdbcMysql51xDataSource(config.jdbcDataSourceConfig))
      .protocalDataConvertor(new JdbcProtocalConvertor())
      .dataConvertor((IDataConvertor<JdbcProtocalData, JdbcSinkReq, BaseSettingContext>) (record, settingContext1) -> {
        JdbcSinkReq jdbcSinkReq = new JdbcSinkReq();
        List<JdbcSinkReq.WriteDataInfo> writeDataInfos = new ArrayList<>();
        jdbcSinkReq.setWriteDataInfos(writeDataInfos);
        JdbcSinkReq.WriteDataInfo writeDataInfo = new JdbcSinkReq.WriteDataInfo();
        writeDataInfos.add(writeDataInfo);
        writeDataInfo.setData(record.getJdbcResult().getData());
        writeDataInfo.setTableName(config.jdbcSinkConfig.getDefaultTableName());
        writeDataInfo.setWhere(record.getJdbcResult().getPrimaryKeysValues());
        writeDataInfo.setWriteModel(config.writeModel);
        return jdbcSinkReq;
      })
      .sink(new JdbcMysql51xSink(config.jdbcSinkConfig))
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

    private JdbcSinkReq.WriteModel writeModel;

  }

}

