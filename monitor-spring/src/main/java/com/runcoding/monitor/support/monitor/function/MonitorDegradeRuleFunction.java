package com.runcoding.monitor.support.monitor.function;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/10 16:08
 * @describe: 自动降级规则配置
 **/
@FunctionalInterface
public interface MonitorDegradeRuleFunction {


    /**
     * @param methodName 执行方法
     * @param duration   执行时间
     * */
    void autoRule(String methodName, long duration);

}
