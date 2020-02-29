package com.runcoding.monitor.web.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author  runcoding
 * @time 2017/10/26.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodMetricInfo {

    private Integer id;

    /**统计日期*/
    private String refDate;

    /**名称*/
    private String  name;

    /**总执行次数*/
    private long cntRequest;

    /**总执行通过次数*/
    private long cntPassRequest;

    /**总执行成功次数*/
    private long cntSuccessRequest;

    /**总执行异常次数*/
    private long cntExceptionRequest;

    /**总执行被阻塞次数*/
    private long cntBlockRequest;

    /**平均相应时间(毫秒)*/
    private long avgRt;

    /**最慢耗时长(毫秒)*/
    private long tardiness;


}
