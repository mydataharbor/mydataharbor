package mydataharbor.datasource;

import com.google.common.util.concurrent.RateLimiter;
import mydataharbor.IDataSource;
import mydataharbor.exception.DataSinkCommonException;
import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带单机限流功能的数据源
 *
 * @auth xulang
 * @Date 2021/5/6
 **/
@Slf4j
public abstract class AbstractRateLimitDataSource<T, S extends BaseSettingContext> implements IDataSource<T, S> {

  private RateLimitConfig rateLimitConfig;

  private static final Map<String, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

  public AbstractRateLimitDataSource(RateLimitConfig rateLimitConfig) {
    this.rateLimitConfig = rateLimitConfig;
    if (rateLimitConfig.getSpeed() == null) {
      throw new DataSinkCommonException("基于限流的数据源请设置speed参数！");
    }
  }

  public RateLimitConfig getRateLimitConfig() {
    return rateLimitConfig;
  }

  @Override
  public Iterable<T> poll(S settingContext) throws TheEndException {
    Collection<T> ts = doPoll(settingContext);
    if (ts.size() == 0)
      return ts;
    RateLimiter rateLimiter = RATE_LIMITER_MAP.get(rateLimitConfig.getRateGroup());
    if (rateLimiter == null) {
      rateLimiter = RateLimiter.create(rateLimitConfig.getSpeed());
      RATE_LIMITER_MAP.put(rateLimitConfig.getRateGroup(), rateLimiter);
    }
    double acquireTime = rateLimiter.acquire(ts.size());
    log.debug("限流耗时：{}", acquireTime);
    return ts;
  }

  public abstract Collection<T> doPoll(S settingContext) throws TheEndException;
}
