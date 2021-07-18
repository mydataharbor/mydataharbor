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
