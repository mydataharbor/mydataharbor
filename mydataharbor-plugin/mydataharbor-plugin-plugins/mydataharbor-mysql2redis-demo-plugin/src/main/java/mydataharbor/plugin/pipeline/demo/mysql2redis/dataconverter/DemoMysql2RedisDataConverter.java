package mydataharbor.plugin.pipeline.demo.mysql2redis.dataconverter;

import mydataharbor.IDataConverter;
import mydataharbor.common.jdbc.source.protocol.JdbcProtocolData;
import mydataharbor.exception.ResetException;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.redis.common.RedisOfStringDataSinkReq;
import mydataharbor.setting.BaseSettingContext;

/**
 * demo测试转换器，依据实际情况修改
 * @author xulang
 * @date 2023/2/2
 */
public class DemoMysql2RedisDataConverter implements IDataConverter<JdbcProtocolData, RedisOfStringDataSinkReq, BaseSettingContext> {
    @Override
    public RedisOfStringDataSinkReq convert(JdbcProtocolData record, BaseSettingContext settingContext) throws ResetException {
        RedisOfStringDataSinkReq redisSinkReqOfString = new RedisOfStringDataSinkReq();
        redisSinkReqOfString.setKey(record.getJdbcResult().getPrimaryKeysValues().get("id").toString());
        redisSinkReqOfString.setValue(JsonUtil.objectToJson(record.getJdbcResult()));
        return redisSinkReqOfString;
    }
}
