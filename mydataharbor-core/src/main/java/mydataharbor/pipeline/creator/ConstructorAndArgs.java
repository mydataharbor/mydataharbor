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
@MyDataHarborMarker(title = "构造器信息")
public class ConstructorAndArgs {

  @MyDataHarborMarker(title = "插件ID")
  private String pluginId;

  @MyDataHarborMarker(title = "类全路径")
  private String clazz;

  @MyDataHarborMarker(title = "类型列表")
  private List<String> argsType;

  @MyDataHarborMarker(title = "json值列表")
  private List<String> argsJsonValue;
}