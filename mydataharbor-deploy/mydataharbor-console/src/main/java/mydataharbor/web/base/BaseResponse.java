package mydataharbor.web.base;

import lombok.Data;

/**
 * @auth xulang
 * @Date 2021/6/26
 **/
@Data
public class BaseResponse<T> {
  private int code;
  private T data;
  private String msg;

  public BaseResponse(int code, T data, String msg) {
    this.code = code;
    this.data = data;
    this.msg = msg;
  }

  public static BaseResponse success(Object data) {
    return new BaseResponse(0, data, "ok");
  }
}
