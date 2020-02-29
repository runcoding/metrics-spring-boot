package com.runcoding.monitor.support.monitor.sentinel;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.runcoding.monitor.support.webhook.dingtalk.DTWebHookProcessor;
import com.runcoding.monitor.support.monitor.function.MonitorDegradeRuleFunction;
import com.runcoding.monitor.support.monitor.function.MonitorFlowRuleFunction;
import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.utils.IpUtils;
import com.runcoding.monitor.web.utils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/03 10:34
 * @describe: Sentinel哨兵规则处理器
 **/
public class SentinelRuleProcessor {

    private static Logger logger = LoggerFactory.getLogger(SentinelRuleProcessor.class);

    /**执行方法白名单{key:执行方法,value:规则截止时间，-1不限制(单位ms)}*/
    private static Map<String,Long> authorityWhite = new ConcurrentHashMap();

    /**执行方法黑名单{key:执行方法,value:规则截止时间，-1不限制(单位ms)}*/
    private static Map<String,Long> authorityBlock = new ConcurrentHashMap();

    /**被自动限流的方法*/
    private static Set<String> methodLimiters      = Collections.synchronizedSet(new HashSet<>());

    /**获取修改流控规则锁*/
    private static ReentrantLock flowRuleLock      = new ReentrantLock();

    /**获取修改降级规则锁*/
    private static ReentrantLock degradeRuleLock   = new ReentrantLock();

    /**自动降级规则配置*/
    public static MonitorDegradeRuleFunction degradeRuleFunction;

    /**自动限流规则配置*/
    public static MonitorFlowRuleFunction flowRuleFunction;

    /**高负载*/
    protected static void highLoadAverageRate(String methodName){
        if(!MonitorConstants.isAutoRule){
            logger.debug("自动流控配置已关闭，当前系统出现高负载");
            return;
        }
        /**请求繁忙*/
        if(!methodLimiters.contains(methodName) && flowRuleLock.tryLock()){
            try {
                methodLimiters.add(methodName);
                if(flowRuleFunction != null){
                    flowRuleFunction.autoRule(methodName);
                }
            }finally {
                flowRuleLock.unlock();
            }
        }
    }

    /**方法降级(限流)，方法执行时间较长，比如大于3s*/
    protected static void methodLimiter(String methodName , Object[] args,long duration){
        if(!MonitorConstants.isAutoRule){
            logger.debug("自动流控配置已关闭，当前{}方法出现响应超时。",methodName);
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName);
            if(!checkEnableSend){
                return;
            }
            /**执行超时,报警通知*/
            DTWebHookProcessor.chatbotSendByMarkdown("超长执行通知",
                    "服务执行时间过长 " +
                            "\n - 服务:"+MonitorConstants.applicationName+"-"+MonitorConstants.applicationEnv
                            +"\n - ip:"+ IpUtils.getIp()+":"+MonitorConstants.applicationPort
                            +"\n - 方法:"+methodName
                            +"\n - 参数:" + MethodUtils.methodArgs(args)
                            +"\n - 当前处理时间(s):"+ duration / MonitorConstants.millisecond
                    , false);
            return;
        }
        if(!methodLimiters.contains(methodName) && degradeRuleLock.tryLock()){
            try {
                methodLimiters.add(methodName);
                if(degradeRuleFunction != null){
                    degradeRuleFunction.autoRule(methodName,duration);
                }
            }finally {
                degradeRuleLock.unlock();
            }
        }
    }


    /**更新流控*/
    public static boolean loadFlowRules(List<FlowRule> rules){
        if(!flowRuleLock.tryLock()) {
            return false;
        }
        try {
            FlowRuleManager.loadRules(rules);
            SentinelRuleProcessor.methodLimiters.addAll(rules.stream().map(r -> r.getResource()).collect(Collectors.toSet()));
        }finally {
            flowRuleLock.unlock();
        }
        return true;
    }

    /**更新降级*/
    public static boolean loadDegradeRules(List<DegradeRule> rules){
        if(!degradeRuleLock.tryLock()) {
            return false;
        }
        try {
            DegradeRuleManager.loadRules(rules);
            SentinelRuleProcessor.methodLimiters.addAll(rules.stream().map(r -> r.getResource()).collect(Collectors.toSet()));
        }finally {
            degradeRuleLock.unlock();
        }
        return true;
    }

    public static Map<String, Long> getAuthorityWhite() {
        return authorityWhite;
    }

    public static void setAuthorityWhite(Map<String, Long> authorityWhite) {
        SentinelRuleProcessor.authorityWhite.clear();
        SentinelRuleProcessor.authorityWhite.putAll(authorityWhite);
    }

    public static Map<String, Long> getAuthorityBlock() {
        return authorityBlock;
    }

    public static void setAuthorityBlock(Map<String, Long> authorityBlock) {
        SentinelRuleProcessor.authorityBlock.clear();
        SentinelRuleProcessor.authorityBlock.putAll(authorityBlock);
    }

}
