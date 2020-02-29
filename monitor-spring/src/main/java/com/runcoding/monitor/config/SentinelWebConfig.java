package com.runcoding.monitor.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.google.common.collect.Maps;
import com.runcoding.monitor.support.monitor.MonitorProcessor;
import com.runcoding.monitor.support.monitor.function.MonitorDegradeRuleFunction;
import com.runcoding.monitor.support.monitor.function.MonitorFlowRuleFunction;
import com.runcoding.monitor.support.monitor.function.MonitorSendFunction;
import com.runcoding.monitor.support.monitor.sentinel.SentinelRuleProcessor;
import com.runcoding.monitor.support.webhook.dingtalk.DTWebHookProcessor;
import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.utils.ErrorMessageUtils;
import com.runcoding.monitor.web.utils.IpUtils;
import com.runcoding.monitor.web.utils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DeadlockLoserDataAccessException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author runcoding
 * @desc 分布式系统的流量防卫组件，Sentinel 把流量作为切入点，从流量控制，熔断降级，系统负载保护等多个维度保护服务的稳定性。
 * https://github.com/alibaba/Sentinel/wiki
 * ArchaiusAutoConfiguration.class @ConditionalOnClass(EnvironmentChangeEvent.class)
 * http://www.scienjus.com/spring-cloud-refresh/
 */
@Configuration
@RefreshScope
@ConditionalOnExpression("${csp.sentinel.default-config.enabled:true}")
public class SentinelWebConfig {

    private static Logger logger = LoggerFactory.getLogger(SentinelWebConfig.class);

    @Value("${spring.application.name:monitor-center}")
    private String appName;

    @Value("${spring.profiles.active:local}")
    private String appEnv;

    @Value("${server.port:8080}")
    private String appPort;

    /**
     * 服务容器运行的最大线程数
     * tomcat最大处理http请求数
     * org.apache.tomcat.util.threads.ThreadPoolExecutor
     * */
    @Value("${server.tomcat.max-threads:200}")
    private long serverMaxThreads;

    @Value("${csp.sentinel.dashboard.server:}")
    private String dashboardServer;

    /**监控白名单方法*/
    @Value("${csp.sentinel.authorityWhite:}")
    private String[] authorityWhite;

    /**监控白名单方法*/
    @Value("${csp.sentinel.authorityBlock:}")
    private String[] authorityBlock;

    /**服务向外暴露端口，供dashboard请求*/
    @Value("${csp.sentinel.api.port:}")
    private String sentinelPort;

    /**配置最高系统加载平均值，是排队到可用处理器的可运行实体数目与可用处理器上可运行实体数目的总和在某一段时间进行平均的结果*/
    @Value("${csp.sentinel.rules.system.systemLoad:-1}")
    private double highestSystemLoad ;

    /**配置最大平均响应时间（max avg rt of all request is 10 s）*/
    @Value("${csp.sentinel.rules.system.qps:-1}")
    private double sentinelSysQps ;

    /**配置最高QPS(max total qps is 1000)*/
    @Value("${csp.sentinel.rules.system.avgRt:-1}")
    private long sentinelSysAvgRt ;

    /**配置最高并行执行线程数(max parallel working thread is 200)*/
    @Value("${csp.sentinel.rules.system.maxThread:-1}")
    private long sentinelSysMaxThread ;

    /**是否自动开启限流*/
    @Value("${csp.sentinel.isAutoRule:true}")
    private boolean sentinelIsAutoRule;

    /**调用请求超时3s,输出服务当前运行日志*/
    @Value("${csp.sentinel.warnTimeoutMillis:3000}")
    private long  warnTimeoutMillis;

    /**是否自动打断执行线程*/
    @Value("${csp.sentinel.isAutoInterrupt:true}")
    private  boolean isAutoInterrupt;

    /**最长的方法执行时长(ms,默认10s)*/
    @Value("${csp.sentinel.maxRunTimeoutMillis:10000}")
    private  long maxRunTimeoutMillis ;

    /**加入黑名单时长(ms,默认3分钟)*/
    @Value("${csp.sentinel.maxBlockTimeMillis:180000}")
    private long maxBlockTimeMillis ;

    /** 超过这个时间(ms,默认0.5s)的请求,将被统计*/
    @Value("${csp.sentinel.maxTardinessMillis:500}")
    private long maxTardinessMillis ;

    /**最大的负载比例(当前正在运行的线程/容器最大数量)，这里的容器默认指tomcat*/
    @Value("${csp.sentinel.maxLoadAverageRate:0.95}")
    private double  maxLoadAverageRate;

    /**接受通知需要被@的人*/
    @Value("${csp.webHook.dingTalk.atMobiles:}")
    private String[] atMobiles;

    /**
     * 每个机器人都不同的accessToken
     * https://open-doc.dingtalk.com/docs/doc.htm?treeId=257&articleId=105735&docType=1
     * */
    @Value("${csp.webHook.dingTalk.accessToken:48b3e9ab5f1bb4833466c2627cbcd9dab29ce67fd667cc44be58ae9b8f6c7202}")
    private String accessToken;

    /**启用发送*/
    @Value("${csp.webHook.dingTalk.enableSend:}")
    private  Boolean enableSend ;

    /**eureka 是否自定义ip*/
    @Value("${eureka.instance.prefer-ip-address:false}")
    private boolean  preferIpAddress;

    /**eureka 自定义ip地址*/
    @Value("${eureka.instance.ip-address:}")
    private String  ipAddress;

    @Resource
    private MonitorProcessor monitorProcessor;

    /**需要报警的异常*/
    private  String[] searchRuntimeExceptions =   new String[] { "java.lang", "org.springframework","feign","com.alibaba.fastjson"};


    private volatile boolean initEvent = false;


    @EventListener()
    public void initEvent(EnvironmentChangeEvent event){
        if(!initEvent){
            /**手动刷新第一次，无响应。但是事件能接受*/
            initialize();
            initEvent = true;
        }
    }


    @PostConstruct
    public void initialize() {
        /** sentinel 配置 */
        System.setProperty(AppNameUtil.APP_NAME,appName);
        if(StringUtils.isNotEmpty(dashboardServer)){
            SentinelConfig.setConfig("csp.sentinel.dashboard.server",dashboardServer);
            SentinelConfig.setConfig("csp.sentinel.api.port",sentinelPort);
            if(!StringUtils.isEmpty(sentinelPort)){
                logger.warn("使用sentinel dashboard，请配置暴露本服务端口。csp.sentinel.api.port=null");
            }
        }
        /**公用配置*/
        MonitorConstants.applicationName     = appName;
        MonitorConstants.applicationEnv      = appEnv;
        MonitorConstants.applicationPort     = appPort;
        MonitorConstants.isAutoRule          = sentinelIsAutoRule;
        MonitorConstants.maxLoadAverageRate  = maxLoadAverageRate;
        MonitorConstants.warnTimeoutMillis   = warnTimeoutMillis;
        MonitorConstants.maxTardinessMillis  = maxTardinessMillis;

        MonitorConstants.isAutoInterrupt     = isAutoInterrupt;
        MonitorConstants.maxRunTimeoutMillis = maxRunTimeoutMillis;
        MonitorConstants.maxBlockTimeMillis  = maxBlockTimeMillis;

        MonitorConstants.serverMaxThreads = serverMaxThreads;
        /**配置机器人*/
        DTWebHookProcessor.setAccessToken(accessToken);
        /**接受通知需要被@的人*/
        LinkedHashSet<String> atMobileSet = new LinkedHashSet<>();
        Collections.addAll(atMobileSet, atMobiles);
        DTWebHookProcessor.setAtMobiles(atMobileSet);
        DTWebHookProcessor.enableSend = enableSend == null ? !StringUtils.endsWith("dev", appEnv) : enableSend;

        /**初始化系统规则*/
        initSystemRule();
        /**初始化黑白名单规则*/
        initAuthorityRule();

        /**自定义异常报警*/
        monitorProcessor.setMonitorSendFunction(chatBotSendLog());
        /**自动限流规则配置*/
        SentinelRuleProcessor.flowRuleFunction   = flowRuleFunction();
        /**自动降级规则配置*/
        SentinelRuleProcessor.degradeRuleFunction = degradeRuleFunction();


        /**Eureka 开启指定ip(替换内网ip)*/
        if(preferIpAddress && StringUtils.isNotBlank(ipAddress)){
            IpUtils.setIp(ipAddress);
        }
    }

    /**初始化系统规则*/
    private  void initSystemRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(highestSystemLoad);
        rule.setAvgRt(sentinelSysAvgRt);
        rule.setQps(sentinelSysQps);
        rule.setMaxThread(sentinelSysMaxThread >0 ? sentinelSysMaxThread : serverMaxThreads);
        SystemRuleManager.loadRules(Collections.singletonList(rule));
    }

    /**初始化黑白名单规则*/
    private  void initAuthorityRule() {
        /**配置白名单*/
        Map<String, Long> currAuthorityWhite = Maps.newHashMap();
        currAuthorityWhite.put("BasicErrorController",-1L);
        currAuthorityWhite.put("GenericConversionService",-1L);
        currAuthorityWhite.put("MonitorController.refresh(0)",-1L);
        currAuthorityWhite.put("CounterServiceBasedSpanMetricReporter",-1L);
        if(authorityWhite != null && authorityWhite.length >0 ){
            for (int i = 0; i < authorityWhite.length; i++) {
                String[] whiteRuleInfo = authorityWhite[i].split("\\|");
                String   methodName    = whiteRuleInfo[0];
                Long     timeOut       = whiteRuleInfo.length >1 ? Long.valueOf(whiteRuleInfo[1]) : -1L;
                currAuthorityWhite.put(methodName,timeOut);
            }
        }
        SentinelRuleProcessor.setAuthorityWhite(currAuthorityWhite);
        /**配置黑白名单*/
        Map<String, Long> currAuthorityBlock = Maps.newHashMap();
        if(authorityBlock != null && authorityBlock.length >0 ){
            for (int i = 0; i < authorityBlock.length; i++) {
                String    blockRule    = authorityBlock[i];
                String[] blockRuleInfo = blockRule.split("\\|");
                Long     timeOut       = blockRuleInfo.length >1 ? Long.valueOf(blockRuleInfo[1]) : -1L;
                String   methodName    = blockRuleInfo[0];
                currAuthorityBlock.put(methodName,timeOut);
            }
        }
        SentinelRuleProcessor.setAuthorityBlock(currAuthorityBlock);
    }


    /**自动限流规则配置*/
    public MonitorFlowRuleFunction flowRuleFunction(){
        return (methodName -> {
            /**自动流控：允许1s，通过服务处理线程最大的一半*/
            FlowRule rule = new FlowRule();
            rule.setResource(methodName);
            rule.setCount(MonitorConstants.serverMaxThreads * 0.5);
            rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
            // 匀速器模式下，设置了 QPS 为 5，则请求每 200 ms 允许通过 1 个 （发生拦截后是直接拒绝，还是排队等待，还是慢启动模式）
            rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
            // 如果更多的请求到达，这些请求会被置于虚拟的等待队列中。
            // 等待队列有一个 max timeout(ms)，如果请求预计的等待时间超过这个时间会直接被 block、
            rule.setMaxQueueingTimeMs(1000);
            List<FlowRule> rules = FlowRuleManager.getRules();
            rules.add(rule);
            FlowRuleManager.loadRules(rules);
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend("系统负载");
            if(!checkEnableSend){
                return;
            }
            /**负载报警通知*/
            DTWebHookProcessor.chatbotSendByMarkdown("系统负载警告>95%",
                    "系统负载警告>95% " +
                            "\n - 服务:"+MonitorConstants.applicationName+"-"+MonitorConstants.applicationEnv
                            +"\n - ip:"+IpUtils.getIp()+":"+MonitorConstants.applicationPort
                    ,false);
        });
    }

    /**自动降级规则配置*/
    public MonitorDegradeRuleFunction degradeRuleFunction(){
        return ((methodName ,duration) -> {
            DegradeRule rule = new DegradeRule();
            rule.setResource(methodName);
            /**当资源的平均响应时间超过阈值3000ms*/
            rule.setCount(3000);
            rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
            /**降级时间2s(2s时间都会进入阻塞)*/
            rule.setTimeWindow(2);
            List<DegradeRule> rules = DegradeRuleManager.getRules();
            rules.add(rule);
            DegradeRuleManager.loadRules(rules);
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName);
            if(!checkEnableSend){
                return;
            }
            /**降级报警通知*/
            DTWebHookProcessor.chatbotSendByMarkdown("自动降级",
                    "接口被自动降级了 " +
                            "\n - 服务:"+MonitorConstants.applicationName+"-"+MonitorConstants.applicationEnv
                            +"\n - ip:"+IpUtils.getIp()+":"+MonitorConstants.applicationPort
                            +"\n - 方法:"+methodName
                            +"\n - 当前处理时间(s):"+ duration / MonitorConstants.millisecond
                            , false);
        });
    }

    /**
     * 自定义异常
     * @默认java.lang 和 org.springframework 异常不吞没
     * */
    public MonitorSendFunction chatBotSendLog(){
        return ((methodName, t, args) -> {
            if(!(t instanceof  RuntimeException)){
                return;
            }
            /**处理RuntimeException异常*/
            String throwablePackageName = t.getClass().getPackage().getName();
            if(StringUtils.startsWithAny(throwablePackageName, searchRuntimeExceptions)){
                boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName + t.getClass().getSimpleName());
                if(!checkEnableSend){
                    return;
                }
                /**是否死锁*/
                boolean  isDeadlock = t instanceof DeadlockLoserDataAccessException;
                /**运行时异常*/
                DTWebHookProcessor.chatbotSendByMarkdown("运行时异常报警",
                       "服务在运行时出现了异常，请即时处理"
                               +"\n - 服务:"+appName+"-"+ appEnv
                               +"\n - ip:"+IpUtils.getIp()+":"+MonitorConstants.applicationPort
                               +"\n - 方法:"+methodName
                               +"\n - 参数:"+ MethodUtils.methodArgs(args)
                               + (isDeadlock ? "\n - 死锁： 当前执行出现了死锁" : "")
                               +"\n - error:"+ ErrorMessageUtils.getMessage(t),
                       false);
           }else{
                if(logger.isDebugEnabled()){
                    logger.debug("服务允许时出现了未通知异常",t);
                }
            }
        });
    }






}