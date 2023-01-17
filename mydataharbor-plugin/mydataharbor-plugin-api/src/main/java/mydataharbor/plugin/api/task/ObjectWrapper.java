package mydataharbor.plugin.api.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper implements Serializable {
    /**
     * 数据生成时间
     */
    private Long time;
    /**
     * 数据对象
     */
    private Serializable obj;
}