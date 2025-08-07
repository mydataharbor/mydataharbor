/*
 * 版权所有 (C) [2020] [xulang 1053618636@qq.com]
 *
 * 此程序是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证第3版或
 * （根据您的选择）任何更高版本重新分发和/或修改它。
 *
 * 此程序基于希望它有用而分发，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。详见 GNU 通用公共许可证。
 *
 * 您应该已经收到 GNU 通用公共许可证的副本。如果没有，请参阅
 * <http://www.gnu.org/licenses/>.
 *
 */


package mydataharbor.datasource;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.IDataSource;
import mydataharbor.exception.DataSinkCommonException;
import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.util.concurrent.RateLimiter;

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