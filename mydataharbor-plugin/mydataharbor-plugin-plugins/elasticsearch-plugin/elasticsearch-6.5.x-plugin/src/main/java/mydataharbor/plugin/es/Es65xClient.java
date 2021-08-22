package mydataharbor.plugin.es;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.sink.EsSinkConfig;
import mydataharbor.sink.EsWriteReq;
import mydataharbor.sink.es.IEsClient;
import mydataharbor.sink.exception.EsException;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by xulang on 2021/7/27.
 */
@Slf4j
public class Es65xClient implements IEsClient {

  private RestHighLevelClient restHighLevelClient;

  private EsSinkConfig esSinkConfig;

  public Es65xClient(EsSinkConfig esSinkConfig) {
    this.esSinkConfig = esSinkConfig;
    HttpHost[] httpHosts = esSinkConfig.getEsIpPort().stream().map(str -> new HttpHost(str.split(":")[0], Integer.parseInt(str.split(":")[1]))).toArray(HttpHost[]::new);
    this.restHighLevelClient = new RestHighLevelClient(RestClient.builder(httpHosts)
      .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
        .setConnectTimeout((int) esSinkConfig.getConnectTimeOut())
        .setSocketTimeout((int) esSinkConfig.getSocketTimeOut()))
      .setMaxRetryTimeoutMillis(10000));
  }

  @Override
  public boolean checkIndexExist(String index) {
    GetRequest getRequest = new GetRequest(index);
    try {
      return restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new EsException("检查索引是否存在时发生异常！", e);
    }
  }

  @Override
  public void createIndex(String index, Map settings, Map mapping) {
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
    createIndexRequest.settings(settings);
    createIndexRequest.mapping("_doc", mapping);
    try {
      restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new EsException("创建索引时发生错误！", e);
    }
  }

  @Override
  public Object write(EsWriteReq esWriteReq) throws Exception {
    switch (esWriteReq.getWriteType()) {
      case DELETE:
        DeleteRequest deleteRequest = new DeleteRequest(esSinkConfig.getWriteIndexConfig().getIndexName(), "_doc", esWriteReq.getKey());
        return restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
      case UPDATE:
        UpdateRequest updateRequest = new UpdateRequest(esSinkConfig.getWriteIndexConfig().getIndexName(), "_doc", esWriteReq.getKey()).doc(esWriteReq.getSource());
        return restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
      case INDEX:
        IndexRequest indexRequest = new IndexRequest(esSinkConfig.getWriteIndexConfig().getIndexName(), "_doc", esWriteReq.getKey()).source(esWriteReq.getSource());
        return restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
      default:
        throw new RuntimeException("impossable!");
    }
  }

  @Override
  public Object batchWrite(List<EsWriteReq> esWriteReqs) throws Exception {
    BulkRequest bulkRequest = new BulkRequest();
    for (EsWriteReq esWriteReq : esWriteReqs) {
      DocWriteRequest docWriteRequest = null;
      switch (esWriteReq.getWriteType()) {
        case DELETE:
          docWriteRequest = new DeleteRequest(esSinkConfig.getWriteIndexConfig().getIndexName(), "_doc", esWriteReq.getKey());
          break;
        case UPDATE:
          docWriteRequest = new UpdateRequest(esSinkConfig.getWriteIndexConfig().getIndexName(), "_doc", esWriteReq.getKey()).doc(esWriteReq.getSource());
          break;
        case INDEX:
          docWriteRequest = new IndexRequest(esSinkConfig.getWriteIndexConfig().getIndexName(), "_doc", esWriteReq.getKey()).source(esWriteReq.getSource());
          break;
        default:
          throw new RuntimeException("impossable!");
      }
      bulkRequest.add(docWriteRequest);
    }
    return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
  }

  @Override
  public void close() {
    if (restHighLevelClient != null) {
      try {
        restHighLevelClient.close();
      } catch (IOException e) {
        log.error("关闭es客户端发生异常！", e);
      }
    }
  }
}
