package mydataharbor.config;

import mydataharbor.classutil.classresolver.MyDataHarborMarker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xulang on 2021/8/22.
 */
public abstract class AbstractConfig {

  @MyDataHarborMarker(title = "其它配置", des = "mydataharbor预留给各组件的额外存储空间", require = false)
  private Map<String, Object> extStore = new HashMap<>();

  public void setExtStore(Map<String, Object> extStore) {
    this.extStore = extStore;
  }

  public Map<String, Object> getExtStore() {
    return extStore;
  }
}
