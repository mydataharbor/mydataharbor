package mydataharbor.source.jdbc;

import lombok.Data;

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
   * 数据
   */
  private Map<String, Object> data;

  /**
   * 时间标识
   */
  private Object timeFlag;

}
