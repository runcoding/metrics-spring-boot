package com.runcoding.monitor.support.monitor.function;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/04 16:08
 * @describe: 警告机器人
 **/
@FunctionalInterface
public interface MonitorSendFunction {

    /**发送异常机器人处理业务*/
    void chatBotSendLog(String methodName, Throwable t, Object[] args);

}
