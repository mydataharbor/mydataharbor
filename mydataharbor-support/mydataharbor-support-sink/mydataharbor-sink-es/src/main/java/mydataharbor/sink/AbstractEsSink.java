package mydataharbor.sink;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.IDataSink;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.es.IEsClient;
import mydataharbor.sink.exception.EsException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

/**
 * es写入器
 * Created by xulang on 2021/7/26.
 */
@Slf4j
public abstract class AbstractEsSink implements IDataSink<EsWriteReq, BaseSettingContext> {


  private IEsClient esClient;

  public AbstractEsSink(EsSinkConfig esSinkConfig) {
    IEsClient esClient = initEsClient(esSinkConfig);
    this.esClient = esClient;
    if (esSinkConfig.getWriteIndexConfig().isAutoCreate()) {
      //创建索引
      if (!esClient.checkIndexExist(esSinkConfig.getWriteIndexConfig().getIndexName())) {
        synchronized (AbstractEsSink.class) {
          if (!esClient.checkIndexExist(esSinkConfig.getWriteIndexConfig().getIndexName())) {
            esClient.createIndex(esSinkConfig.getWriteIndexConfig().getIndexName(), esSinkConfig.getWriteIndexConfig().getSettings(), esSinkConfig.getWriteIndexConfig().getMapping());
          }
        }
      }
    }
  }

  /**
   * 初始化esClient客户端
   *
   * @param esSinkConfig
   * @return
   */
  public abstract IEsClient initEsClient(EsSinkConfig esSinkConfig);


  @Override
  public WriterResult write(EsWriteReq record, BaseSettingContext settingContext) throws ResetException {
    WriterResult.WriterResultBuilder writerResultBuilder = WriterResult.builder();
    try {
      Object writeResult = esClient.write(record);
      log.info("写入结果：{}", writeResult);
      writerResultBuilder.writeReturn(writeResult);
    } catch (Exception e) {
      if (e instanceof ConnectException) {
        throw new ResetException("连接异常", e);
      } else {
        throw new EsException("请求es发生异常", e);
      }
    }
    return writerResultBuilder.success(true).commit(true).msg("ok").build();
  }

  @Override
  public WriterResult write(List<EsWriteReq> records, BaseSettingContext settingContext) throws ResetException {
    WriterResult.WriterResultBuilder writerResultBuilder = WriterResult.builder();
    try {
      Object writeResult = esClient.batchWrite(records);
      writerResultBuilder.writeReturn(writeResult);
    } catch (Exception e) {
      if (e instanceof ConnectException) {
        throw new ResetException("连接异常", e);
      } else {
      }
      throw new EsException("", e);
    }
    return writerResultBuilder.success(true).commit(true).msg("ok").build();
  }

  @Override
  public void close() throws IOException {
    if (esClient != null) {
      esClient.close();
    }
  }
}
