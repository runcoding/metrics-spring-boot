package com.runcoding.monitor.support.monitor.function;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/10 16:08
 * @describe: 自动限流规则配置
 **/
@FunctionalInterface
public interface MonitorFlowRuleFunction {


    void autoRule(String methodName);

}
