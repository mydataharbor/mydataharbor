package mydataharbor.source.jdbc;

import lombok.Data;
import mydataharbor.source.jdbc.config.JdbcSyncModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xulang on 2021/8/19.
 */
@Data
public class JdbcResult {

  /**
   * 位置
   */
  private int position;

  /**
   * 当前消息是哪个模式下产生的数据
   */
  private JdbcSyncModel jdbcSyncModel;

  /**
   * 全部数据
   */
  private Map<String, Object> data;

  /**
   * 主键列->主键value
   */
  private Map<String, Object> primaryKeysValues = new HashMap<>();

  /**
   * 时间标识
   */
  private Object timeFlag;

}
