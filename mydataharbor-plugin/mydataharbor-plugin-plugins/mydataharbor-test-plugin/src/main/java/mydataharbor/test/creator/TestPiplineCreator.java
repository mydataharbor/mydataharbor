package mydataharbor.test.creator;

import lombok.Data;
import mydataharbor.IDataPipline;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.convertor.data.OriginalDataConvertor;
import mydataharbor.convertor.protocal.OriginalProtocalDataConvertor;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.plugin.base.creator.AbstractAutoScanPiplineCreator;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.test.datasource.TestDataSource;
import mydataharbor.test.sink.TestSink;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * Created by xulang on 2021/8/10.
 */
@Extension
public class TestPiplineCreator extends AbstractAutoScanPiplineCreator<TestPiplineCreator.TestPiplineCretorConfig, BaseSettingContext> implements  ExtensionPoint {


  @Override
  public String type() {
    return "test管道创建器";
  }

  @Override
  public IDataPipline createPipline(TestPiplineCretorConfig config, BaseSettingContext settingContext) throws Exception {
    CommonDataPipline commonDataPipline = CommonDataPipline.builder()
      .dataSource(new TestDataSource(config.total))
      .protocalDataConvertor(new OriginalProtocalDataConvertor())
      .dataConvertor(new OriginalDataConvertor())
      .sink(new TestSink())
      .settingContext(settingContext)
      .build();
    return commonDataPipline;
  }

  @Override
  public String scanPackage() {
    return "mydataharbor.test";
  }

  @Data
  public static class TestPiplineCretorConfig {
    /**
     * 数据总数
     */
    @MyDataHarborMarker(title = "数据总数")
    private Long total;

    /**
     * 枚举测试
     */
    @MyDataHarborMarker(title = "枚举测试属性", defaultValue = "ENUM1")
    private TestEnum testEnum;
  }

  public static enum TestEnum {
    ENUM1, ENUM2, ENUM3
  }
}

