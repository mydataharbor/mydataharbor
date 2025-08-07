/*
 * 版权所有 (C) [2020] [xulang 1053618636@qq.com]
 *
 * 此程序是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证第3版或
 * （根据您的选择）任何更高版本重新分发和/或修改它。
 *
 * 此程序基于希望它有用而分发，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。详见 GNU 通用公共许可证。
 *
 * 您应该已经收到 GNU 通用公共许可证的副本。如果没有，请参阅
 * <http://www.gnu.org/licenses/>.
 *
 */


package mydataharbor.pipeline.creator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MyDataHarborMarker(title = "通用构建器参数配置")
public class CommonPipelineCreatorConfig {

  @MyDataHarborMarker(title = "数据源")
  private ConstructorAndArgs dataSource;

  @MyDataHarborMarker(title = "协议转换器")
  private ConstructorAndArgs protocolDataConverter;

  @MyDataHarborMarker(title = "校验过滤器",des = "过滤器需要继承AbstractDataChecker")
  private List<ConstructorAndArgs> dataCheckers;

  @MyDataHarborMarker(title = "数据转换器", des = "将协议数据转换成写入器可以识别的数据类型")
  private ConstructorAndArgs dataConverter;

  @MyDataHarborMarker(title = "数据写入器")
  private ConstructorAndArgs dataSink;

  @MyDataHarborMarker(title = "settingContext类型", require = false, des = "一般来说不需要指定")
  private String settingContextClazz;

  @MyDataHarborMarker(title = "settingContext值", require = false, des = "json类型的值")
  private String settingContextJsonValue;

}