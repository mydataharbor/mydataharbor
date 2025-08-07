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


package mydataharbor.web.advice;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.web.base.BaseResponse;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class SpringControllerAdvice {
  /**
   * 应用到所有被@RequestMapping注解的方法，在其执行之前初始化数据绑定器
   *
   * @param binder
   */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
  }


  /**
   * 全局异常捕捉处理
   *
   * @param ex
   * @return
   */
  @ResponseBody
  @ExceptionHandler(value = Exception.class)
  public BaseResponse errorHandler(Exception ex) {
    log.error("发生异常", ex);
    return new BaseResponse(-1,null,ex.getMessage());
  }

}