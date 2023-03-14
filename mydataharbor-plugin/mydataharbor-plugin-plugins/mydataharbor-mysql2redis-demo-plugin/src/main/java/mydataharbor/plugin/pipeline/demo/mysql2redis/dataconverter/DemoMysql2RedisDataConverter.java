package mydataharbor.plugin.pipeline.demo.mysql2redis.dataconverter;

import lombok.Data;
import mydataharbor.IDataConverter;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.common.jdbc.source.protocol.JdbcProtocolData;
import mydataharbor.exception.ResetException;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.redis.common.RedisOfStringDataSinkReq;
import mydataharbor.setting.BaseSettingContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * demo测试转换器，依据实际情况修改
 *
 * @author xulang
 * @date 2023/2/2
 */
public class DemoMysql2RedisDataConverter implements IDataConverter<JdbcProtocolData, RedisOfStringDataSinkReq, BaseSettingContext> {

    private DemoMysql2RedisDataConverterConfig converterConfig;

    public DemoMysql2RedisDataConverter(DemoMysql2RedisDataConverterConfig converterConfig) {
        this.converterConfig = converterConfig;
    }

    @Override
    public RedisOfStringDataSinkReq convert(JdbcProtocolData record, BaseSettingContext settingContext) throws ResetException {
        RedisOfStringDataSinkReq redisSinkReqOfString = new RedisOfStringDataSinkReq();
        List<String> values = new ArrayList<>();
        for (String primaryKeyName : converterConfig.getPrimaryKeyNames()) {
            values.add(record.getJdbcResult().getPrimaryKeysValues().get(primaryKeyName).toString());
        }
        String key = StringUtils.join(values, converterConfig.separator);
        redisSinkReqOfString.setKey(converterConfig.keyPrefix+key);
        redisSinkReqOfString.setValue(JsonUtil.objectToJson(record.getJdbcResult()));
        return redisSinkReqOfString;
    }

    @Data
    public static class DemoMysql2RedisDataConverterConfig {
        @MyDataHarborMarker(title = "写入到Redis中key前缀")
        private String keyPrefix;
        @MyDataHarborMarker(title = "需要获取的主键名称，用于拼接")
        private String[] primaryKeyNames;
        @MyDataHarborMarker(title = "多主键join分隔符")
        private String separator;
    }
}
