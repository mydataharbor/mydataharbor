package mydataharbor.test.checkers;

import mydataharbor.AbstractDataChecker;
import mydataharbor.IProtocolData;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.setting.BaseSettingContext;

/**
 * 校验通过校验器，用于测试
 * @author xulang
 * @date 2023/3/9
 */
@MyDataHarborMarker(title = "校验通过校验器，用于测试")
public class TestPassChecker1 extends AbstractDataChecker <IProtocolData, BaseSettingContext> {

    public TestPassChecker1(String p){

    }

    @Override
    protected CheckResult doCheck(IProtocolData protocolData, BaseSettingContext settingContext) {
        return CheckResult.builder().pass(true).msg("ok").build();
    }

}
