package mydataharbor.plugin.base.creator;

import mydataharbor.IDataPipelineCreator;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.setting.BaseSettingContext;

/**
 * @author xulang
 * @date 2023/2/2
 */
public abstract class AbstractDataPipelineCreator<C, S extends BaseSettingContext> implements IDataPipelineCreator<C, S> {
    @Override
    public <T> T parseJson(String json, Class<T> clazz) {
        return JsonUtil.jsonToObject(json, clazz);
    }
}
