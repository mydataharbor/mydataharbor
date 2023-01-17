package mydataharbor.web.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务监控信息
 * @author xulang
 * @date 2022/12/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskMonitorInfo {
    /**
     * 拉取异常次数
     */
    private Long pollErrorCount = 0L;
    /**
     * 拉取数据总数
     */
    private Long recordCount = 0L;
    /**
     * 协议转换成功记录数
     */
    private Long protocolConvertSuccessCount = 0L;
    /**
     * 协议转换失败记录数
     */
    private Long protocolConvertErrorCount = 0L;
    /**
     * 校验器通过记录数
     */
    private Long checkerSuccessCount = 0L;
    /**
     * 校验器失败记录数
     */
    private Long checkerErrorCount = 0L;
    /**
     * 数据转换成功记录数
     */
    private Long dataConvertSuccessCount = 0L;
    /**
     * 数据转换失败记录数
     */
    private Long dataConvertErrorCount = 0L;
    /**
     * 数据写入成功记录数
     */
    private Long writeSuccessCount = 0L;
    /**
     * 数据写入失败记录数
     */
    private Long writeErrorCount = 0L;
    /**
     * 总耗时
     */
    private Long useTime = 0L;
    /**
     * 最后一次运行时间
     */
    private Long lastRunTime = 0L;
}
