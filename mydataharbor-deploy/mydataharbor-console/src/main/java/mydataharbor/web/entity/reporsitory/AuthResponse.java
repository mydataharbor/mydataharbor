package mydataharbor.web.entity.reporsitory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by xulang on 2021/8/26.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  private boolean success;

  private String msg;
}
