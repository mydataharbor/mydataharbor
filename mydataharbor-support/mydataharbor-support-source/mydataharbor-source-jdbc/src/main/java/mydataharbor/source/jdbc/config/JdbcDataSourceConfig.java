package mydataharbor.source.jdbc.config;

import lombok.Data;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.datasource.RateLimitConfig;

import java.util.List;

@Data
public class JdbcDataSourceConfig extends RateLimitConfig {

  public static final String MILLI_SECOND = "MILLI_SECOND";

  public static final String SECOND = "SECOND";

  @MyDataHarborMarker(title = "jdbc连接url")
  private String url;

  @MyDataHarborMarker(title = "用户名")
  private String username;

  @MyDataHarborMarker(title = "密码")
  private String password;

  @MyDataHarborMarker(title = "任务创建时和数据库的连接数", defaultValue = "1")
  private Integer initialSize = 1;

  @MyDataHarborMarker(title = "获取数据的sql语句")
  private String querySql;

  @MyDataHarborMarker(title = "单次poll最大返回数量", defaultValue = "200")
  private Integer maxPollRecords = 200;

  @MyDataHarborMarker(title = "模式，增量/全量/先全量后增量", defaultValue = "INCREMENT_AFTER_COMPLETE", des = "默认先全量，后增量")
  private JdbcSyncModel model = JdbcSyncModel.INCREMENT_AFTER_COMPLETE;

  @MyDataHarborMarker(title = "统计sql", require = false, des = "全量模式下用户可以提供统计sql，用于系统计算迁移进度，非必须")
  private String countSql;

  @MyDataHarborMarker(title = "时间滚动字段", require = false, des = "当处于增量模式下，或者处于先全量后增量模式下，该字段必须提供。该字段需要可以表示该条记录的更新时间，时间类型。建议该字段添加索引！")
  private String rollingFieldName;

  @MyDataHarborMarker(title = "开始时间", des = "当增量和先全量后增量模式下必须指定")
  private Object startTime;

  @MyDataHarborMarker(title = "时间滚动字段的格式", des = "如 yyyy-MM-dd HH:mm:ss.SSS，如果为数值类型，秒填写" + SECOND + ",毫秒填写：" + MILLI_SECOND)
  private String timeFormat;

  @MyDataHarborMarker(title = "主键列", des = "传递给下游，能唯一标识数据库中记录的唯一性，支持联合主键")
  private List<String> primaryKeys;

}