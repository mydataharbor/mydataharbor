package mydataharbor.sink;

import lombok.Data;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;

import java.util.Map;

/**
 * 初始化索引配置
 * Created by xulang on 2021/7/27.
 */
@Data
public class WriteIndexConfig {

  @MyDataHarborMarker(title = "索引名称", des = "需要写入的索引名称")
  private String indexName;

  @MyDataHarborMarker(title = "是否自动创建索引", des = "在任务创建的时候是否按setting和mapping的配置提前创建好索引")
  private boolean autoCreate = false;

  @MyDataHarborMarker(title = "es settings配置")
  private Map<String, Object> settings;

  @MyDataHarborMarker(title = "es mapping配置")
  private Map<String, Object> mapping;


}
