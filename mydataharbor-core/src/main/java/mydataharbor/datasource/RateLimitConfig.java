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


package mydataharbor.datasource;

import lombok.Data;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;

/**
 * 限流配置
 *
 * @auth xulang
 * @Date 2021/5/6
 **/

@Data
public class RateLimitConfig {

  @MyDataHarborMarker(title = "限流组", des = "默认系统会设置为taskid，则表示在同jvm下的每个task的总速度，用户也可以自己设置", require = false)
  private String rateGroup;

  @MyDataHarborMarker(title = "限速 /s")
  private Long speed;

}