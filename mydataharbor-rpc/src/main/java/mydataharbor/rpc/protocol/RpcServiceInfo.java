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


package mydataharbor.rpc.protocol;

import mydataharbor.rpc.util.JsonUtil;

import java.io.Serializable;
import java.util.Objects;

public class RpcServiceInfo implements Serializable {
  // interface name
  private String serviceName;
  // service version
  private String version;

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RpcServiceInfo that = (RpcServiceInfo) o;
    return Objects.equals(serviceName, that.serviceName) &&
      Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceName, version);
  }

  public String toJson() {
    String json = JsonUtil.objectToJson(this);
    return json;
  }

  @Override
  public String toString() {
    return toJson();
  }
}