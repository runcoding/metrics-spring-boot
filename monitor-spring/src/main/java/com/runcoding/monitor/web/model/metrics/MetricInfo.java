package com.runcoding.monitor.web.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author  xukai
 * @time 2017/10/26.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricInfo {

    private Integer id;

    /**
     * 统计日期(yyyy-mm-dd)
     */
    private String refDate;

    /**本次记录名称*/
    private String  name;

    /**本次记录次数*/
    private long count;

    /**近一分钟tps*/
    private double minuteRate;

    /**平均耗时均值(s)*/
    private double mean;

}
