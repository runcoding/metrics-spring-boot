package com.runcoding.monitor.support.monitor.sentinel;

import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.web.model.MonitorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/31 11:24
 * @describe: 哨兵监控处理器
 **/
public class SentinelProcessor {

    private static Logger  logger =LoggerFactory.getLogger(SentinelProcessor.class);

    /**正在处理的线程数*/
    private static AtomicLong processThreads   = new AtomicLong(0);

    /**高负载流控*/
    public static void highLoadSentinelAutoRule(boolean isWhiteMethod ,boolean isThreadRunningMethod, String methodName){
        if(!isThreadRunningMethod){
            return;
        }
        long currProcessThreads = processThreads.getAndIncrement();
        if( isWhiteMethod ||  (currProcessThreads / MonitorConstants.serverMaxThreads) < MonitorConstants.maxLoadAverageRate){
           return;
        }
        /**正常处理线程大于95%时,自动进入流控*/
        SentinelRuleProcessor.highLoadAverageRate(methodName);
    }

    /***执行时间大于等于3s,自动限流 */
    public static void timeOutSentinelAutoRule(boolean isWhiteMethod ,boolean isThreadRunningMethod ,
                                               String methodName , Object[] args, long duration ){
       try{
           /**记录最长执行时间*/
           MetricProcessor.signTardiness(methodName,duration);
           if(duration < MonitorConstants.warnTimeoutMillis){
               return;
           }
           warnLogger(methodName,duration);

           if(isWhiteMethod){
               return;
           }

           if(isThreadRunningMethod){
              /**当前方法为执行记录初始方法*/
              SentinelRuleProcessor.methodLimiter(methodName,args,duration);
           }
       }catch (Exception e){
           logger.error("记录执行时间错误:",e);
       }
    }

    public static void destroy(){
        processThreads.getAndDecrement();
    }

    /***打印日志*/
    private static void warnLogger(String methodName, long duration) {

        OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();

        /**后一分钟内系统加载平均值。*/
        double systemLoadAverage   = system.getSystemLoadAverage();

        logger.warn("【执行】[method]{},当前处理时长:{},系统负载:{}",methodName,duration/MonitorConstants.millisecond,systemLoadAverage);
    }

    /**是否高负载*/
    public static boolean isHighLoad(){
        return (processThreads.get() / MonitorConstants.serverMaxThreads) > MonitorConstants.maxLoadAverageRate;
    }

}
