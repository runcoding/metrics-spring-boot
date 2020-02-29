package com.runcoding.monitor.support.metric;

import com.runcoding.monitor.support.monitor.sentinel.SentinelProcessor;
import com.runcoding.monitor.support.monitor.sentinel.SentinelRuleProcessor;
import com.runcoding.monitor.web.model.MonitorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author runcoding
 * @desc 统计
 */
public class MetricProcessor {

    private static Logger logger = LoggerFactory.getLogger(MetricProcessor.class);

    /**方法最慢耗时长(ms){key:方法名称,value:执行时长}*/
    private static Map<String,Long> tardinessMap = new ConcurrentHashMap<>();

    /**http线程正在处理什么方法{key:执行线程id,value:{key:方法名称,value:执行时长}}*/
    private static Map<Long,MethodNodeInfo> threadRunningMethods = new ConcurrentHashMap<>();

    /**
     * 记录执行时间大0.5s的方法
     * @param methodName 执行方法名称
     * @param duration   方法执行耗时ms 标记
     * */
    public static void signTardiness(String methodName , Long duration){
        if(duration > MonitorConstants.maxTardinessMillis){
            Long pastDuration = tardinessMap.getOrDefault(methodName,MonitorConstants.millisecond);
            if(duration > pastDuration){
                tardinessMap.put(methodName,duration);
            }
        }
    }

    /**删除并获取方法最长执行时长,返回毫秒*/
    public static Long removeTardinessMethod(String methodName){
        return tardinessMap.remove(methodName);
    }

    /**当前方法被多少个线程正在执行*/
    public static Map<String,Integer> methodThreadRunningCnt(){
        Map<String,Integer> methodThreadRunningCnt = new HashMap<>(threadRunningMethods.size());
        threadRunningMethods.forEach((currentThreadId,methodInfo)->{
            String methodName = methodInfo.methodName;
            Integer cnt = methodThreadRunningCnt.getOrDefault(methodName, 0);
            cnt++;
            methodThreadRunningCnt.put(methodName,cnt);
        });
        return methodThreadRunningCnt;
    }


    /**
     * 当前执行线程最先执行controller还是service。只拦截一次
     * 1. http接口执行线程执行Controller --> service
     * 2. job || dubbo 执行service
     * */
    public static boolean threadRunningMethod(String methodName ,long execMethodStartTime ,boolean isController){
        /**记录执行线程*/
        Thread thread = Thread.currentThread();
        long currentThreadId = thread.getId();
        if(isController || !threadRunningMethods.containsKey(currentThreadId)){
            if(!MonitorConstants.isAutoInterrupt){
                thread = null;
            }
            threadRunningMethods.put(currentThreadId,new MethodNodeInfo(methodName,execMethodStartTime,thread));
            return true;
        }
        return false;
    }

    /**清除哪个线程在处理什么方法*/
    public static  void destroy(){
        long currentThreadId = Thread.currentThread().getId();
        threadRunningMethods.remove(currentThreadId);
    }

    /**当前线程能否发送通知*/
    public static boolean enableSend(){
        long currentThreadId = Thread.currentThread().getId();
        MethodNodeInfo methodNodeInfo = threadRunningMethods.get(currentThreadId);
        if(methodNodeInfo != null && !methodNodeInfo.isSend){
            methodNodeInfo.isSend = true;
            return true;
        }
        return false;
    }


    /**打断长时间运行的线程*/
    public static  void interruptRunningTimeout(){
        if(!MonitorConstants.isAutoInterrupt){
            return;
        }
        /**判断是否高负载，运行中的线程>95%*/
        if(!SentinelProcessor.isHighLoad()){
           return;
        }
        logger.info("开始打断执行时长超过10s的线程");
        long currentTimeMillis = System.currentTimeMillis();
        threadRunningMethods.forEach((threadId,methodNode)->{
            String methodName = methodNode.methodName;
            /**打断执行时间超过10s的服务*/
            if(( currentTimeMillis - methodNode.execMethodStartTime) >  MonitorConstants.maxRunTimeoutMillis){
               try {
                   Thread thread = methodNode.thread;
                   if(thread != null && !thread.isInterrupted()){
                       logger.warn("打断threadId={}，执行的{}服务:",thread.getId(),methodName);
                       thread.interrupt();
                       Map<String, Long> authorityBlock = SentinelRuleProcessor.getAuthorityBlock();
                       /**加入黑名单3分钟*/
                       authorityBlock.put(methodName,currentTimeMillis + MonitorConstants.maxBlockTimeMillis);
                   }
               }catch (Exception e){
                   logger.warn("打断运行超过10s线程出现异常：{}-{}",e.toString(),e.getMessage());
               }
            }
        });
    }


    static class MethodNodeInfo {

        /**方法名称*/
        private String  methodName;

        /**开始执行时间(ms)*/
        private Long    execMethodStartTime;

        /**执行线程*/
        private Thread thread;

        /**当前是否发送通知*/
        private boolean isSend;

        public MethodNodeInfo(String methodName, Long execMethodStartTime, Thread thread) {
            this.methodName = methodName;
            this.execMethodStartTime = execMethodStartTime;
            this.thread = thread;
        }
    }

}
