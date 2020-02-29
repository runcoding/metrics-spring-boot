package com.runcoding.monitor.web.model.container;

import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2018/4/27 14:13
 * @description 方法监控执行信息
 * Copyright (C), 2017-2018,
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerMethodMonitorInfo {

    /**被监控了的方法*/
    private Set<String> methodNames;

    /**监控的方法*/
    private String   proposalMethodName;

    /**监控方法执行指标信息*/
    private List<MethodMetricInfo> proposalMetricList;


}
