package mydataharbor.sink.jdbc;

import lombok.Data;
import mydataharbor.sink.jdbc.config.WriteModel;

import java.util.List;
import java.util.Map;

/**
 * Created by xulang on 2021/8/19.
 */
@Data
public class JdbcSinkReq {

  /**
   * 支持对多表写入，事务级
   */
  private List<WriteDataInfo> writeDataInfos;

  @Data
  public static class WriteDataInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 写入数据
     */
    private Map<String, Object> data;

    /**
     * where条件
     */
    private Map<String, Object> where;

    /**
     * 更新模式
     */
    private WriteModel writeModel = WriteModel.UPSET;
  }


}
