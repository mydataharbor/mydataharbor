package mydataharbor;

import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 数据写入器
 *
 * @auth xulang
 * @Date 2021/4/29
 **/
public interface IDataSink<R, S extends BaseSettingContext> extends Closeable, IData {

  /**
   * sink名称
   *
   * @return
   */
  String name();

  /**
   * 单条写入，当该接口抛出任何异常时，都不会提交改数据
   * 如果开启多线程处理，单条提交，则需要idatasink的write相关方法线程安全
   *
   * @param record
   * @return
   */
  WriterResult write(R record, S settingContext) throws ResetException;

  /**
   * 批量写入
   * 当该接口抛出任何异常时，改条数据都不会被提交
   *
   * @param records
   * @return
   */
  WriterResult write(List<R> records, S settingContext) throws ResetException;

  default Type getRType() {
    return getTypeByIndex(0, "R", IDataSink.class);
  }

  default Type getSType() {
    return getTypeByIndex(1, "S", IDataSink.class);
  }

  /**
   * 写入结果
   */
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WriterResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 是否提交
     */
    private boolean commit;

    /**
     * 失败消息
     */
    private String msg;
  }
}
