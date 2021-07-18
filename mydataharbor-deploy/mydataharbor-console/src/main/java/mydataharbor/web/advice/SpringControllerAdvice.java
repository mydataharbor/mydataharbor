package mydataharbor.web.advice;

import mydataharbor.web.base.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

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