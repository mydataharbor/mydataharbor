package mydataharbor.plugin.pipeline.demo.mysql2redis.creator;

import lombok.Data;
import mydataharbor.IDataPipeline;
import mydataharbor.IDataPipelineCreator;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.common.jdbc.source.config.JdbcDataSourceConfig;
import mydataharbor.pipeline.CommonDataPipeline;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.jdbc.mysql.source.JdbcMysql80xDataSource;
import mydataharbor.plugin.jdbc.source.protocol.JdbcProtocolConvertor;
import mydataharbor.plugin.pipeline.demo.mysql2redis.dataconverter.DemoMysql2RedisDataConverter;
import mydataharbor.plugin.redis.common.RedisDataSinkConfig;
import mydataharbor.plugin.redis.sink.RedisOfStringDataSink;
import mydataharbor.setting.BaseSettingContext;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * Created by xulang on 2021/8/10.
 */
@Extension
public class DemoMysql2RedisPipelineCreator implements IDataPipelineCreator<DemoMysql2RedisPipelineCreator.Mysql2RedisPipelineCreatorConfig, BaseSettingContext>, ExtensionPoint {

    @Override
    public String type() {
        return "我的mysql到redis的数据同步";
    }

    @Override
    public IDataPipeline createPipeline(Mysql2RedisPipelineCreatorConfig config, BaseSettingContext settingContext) throws Exception {
        CommonDataPipeline commonDataPipeline = CommonDataPipeline.builder()
                .dataSource(new JdbcMysql80xDataSource(config.jdbcDataSourceConfig))
                .protocolDataConverter(new JdbcProtocolConvertor())
                .dataConverter(new DemoMysql2RedisDataConverter())
                .sink(new RedisOfStringDataSink(config.redisSinkConfig))
                .settingContext(settingContext)
                .build();
        return commonDataPipeline;
    }

    @Override
    public <T> T parseJson(String json, Class<T> clazz) {
        return JsonUtil.jsonToObject(json, clazz);
    }

    @Data
    public static class Mysql2RedisPipelineCreatorConfig {

        @MyDataHarborMarker(title = "数据源配置")
        private JdbcDataSourceConfig jdbcDataSourceConfig;
        @MyDataHarborMarker(title = "写入源配置")
        private RedisDataSinkConfig redisSinkConfig;

    }

}

