package mydataharbor.test.creator;

import lombok.Data;
import mydataharbor.IDataPipeline;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.converter.data.OriginalDataConverter;
import mydataharbor.converter.protocol.OriginalProtocolDataConverter;
import mydataharbor.pipeline.CommonDataPipeline;
import mydataharbor.plugin.base.creator.AbstractAutoScanPipelineCreator;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.test.checkers.TestPassChecker1;
import mydataharbor.test.datasource.TestDataSource;
import mydataharbor.test.sink.TestSink;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * Created by xulang on 2021/8/10.
 */
@Extension
public class TestPipelineCreator extends AbstractAutoScanPipelineCreator<TestPipelineCreator.TestPipelineCretorConfig, BaseSettingContext> implements  ExtensionPoint {


  @Override
  public String type() {
    return "随机产生数据到丢弃写入器";
  }

  @Override
  public IDataPipeline createPipeline(TestPipelineCretorConfig config, BaseSettingContext settingContext) throws Exception {
    CommonDataPipeline commonDataPipeline = CommonDataPipeline.builder()
      .dataSource(new TestDataSource(config.total))
      .protocolDataConverter(new OriginalProtocolDataConverter())
      .checker(new TestPassChecker1("test"))
      .dataConverter(new OriginalDataConverter())
      .sink(new TestSink())
      .settingContext(settingContext)
      .build();
    return commonDataPipeline;
  }

  @Override
  public String scanPackage() {
    return "mydataharbor.test";
  }

  @Data
  public static class TestPipelineCretorConfig {
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

  public enum TestEnum {
    ENUM1, ENUM2, ENUM3
  }
}

