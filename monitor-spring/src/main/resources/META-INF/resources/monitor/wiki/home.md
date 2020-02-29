## 轻量级流量控制
基于[Sentinel](https://github.com/alibaba/Sentinel/wiki)实现

### 如何使用
步骤一：
```xml
<dependency>
   <groupId>com.runcoding</groupId>
   <artifactId>monitor-spring</artifactId>
   <version>1.0.0-SNAPSHOT</version>
</dependency>
```
步骤二：
```yml
spring:
  application:
    name: monitor-center  # 应用名称
  profiles:
    active: dev           # 运行环境(显示在钉钉消息中)

server:
  tomcat:
    max-threads:  100     # Reactor线程池最大线程数(目前使用的是tomcat)

# 哨兵限流配置支持与spring config结合的热更新方式。通过Eureka服务发现节点。注意刷新时会重启Eureka注册节点。
csp:
  sentinel:
    rules:
      system:            # 配置哨兵平台规则
        systemLoad: 10.0 # 配置最高系统加载平均值，是排队到可用处理器的可运行实体数目与可用处理器上可运行实体数目的总和在某一段时间进行平均的结果
        avgRt: 10000     # 平均RT时间(ms)
        qps:   1000      # 每秒接受处理的请求数
        maxThread: 200   # 最高并行执行线程数
    # http://tool.chinaz.com/Tools/unixtime.aspx
    authorityWhite: MonitorController.job(0)|1536595200000,OrderProducerService.send(2)|-1 #监控方法白名单(Unix时间单位ms,-1不设置过期时间,Unix时间 1536595200000 = 2018-09-11 00:00:00)
    authorityBlock: MonitorController.degrade(0)|-1 #监控黑名单方法
    isAutoRule:   true         # 是否自动开启限流
    maxLoadAverageRate: 0.95   # 最大的负载比例(当前正在运行的线程/容器最大数量)，这里的容器默认指tomcat。超过后会进入打断执行线程判断
    isAutoInterrupt: true      # 是否自动打断执行线程(如果设置为false,maxRunTimeoutMillis和maxBlockTimeMillis配置将失效)
    maxRunTimeoutMillis: 10000 # 最长的方法执行时长(ms,默认10s),超过这个时间执行线程会被打断，并且加入到黑名单中
    maxBlockTimeMillis: 180000 # 加入黑名单时长(ms,默认3分钟)
    warnTimeoutMillis: 3000    # 调用请求超时3s,输出服务当前运行日志
    maxTardinessMillis: 500    # 超过这个时间(ms,默认0.5s)的请求,将在每小时被统计
    api:
     # port: 8099              # 服务向外暴露端口，供dashboard请求(不填不暴露)，目前不建议使用
    dashboard:
     # server: localhost:8090  # dashboard 监控台地址，目前不建议使用
  webHook:
    # 钉钉文档 https://open-doc.dingtalk.com/docs/doc.htm?treeId=257&articleId=105735&docType=1
    dingTalk:
      atMobiles: 15869111000,15869111001 #接受钉钉，通知需要被@的人
      accessToken: 84b2a8576d514dfc59e86038b72d3f0bd9362461f5a9d2267a10a64e98f93637 #钉钉通知机器人

eureka:   # 多节点部署需配置eureka,用做哨兵变更规则

```
> 具体可查看: SentinelWebConfig.java

```java
@Resource
private MonitorProcessor monitorProcessor;

monitorProcessor.setWarnChatBot(chatBotSendLog());

/**自定义异常报警*/
public MonitorSendFunction chatBotSendLog(){
    return ((methodName, t, args) -> {
        if(!(t instanceof  RuntimeException)){
            return;
        }
        /**处理RuntimeException异常*/
        String throwablePackageName = t.getClass().getPackage().getName();
        if(StringUtils.startsWith(throwablePackageName,"java.lang") ||
           StringUtils.startsWith(throwablePackageName,"org.springframework")    ){
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName + t.getClass().getSimpleName());
            if(!checkEnableSend){
                return;
            }
            /**是否死锁*/
            boolean  isDeadlock = t instanceof DeadlockLoserDataAccessException;
            /**运行时异常*/
            DTWebHookProcessor.chatbotSendByMarkdown("运行时异常报警",
                   "## 服务在运行时出现了异常，请即时处理 \n - 服务:"+appName+"-"+ appEnv
                           +"\n - ip:"+HostNameUtil.getIp()
                           +"\n - 方法:"+methodName
                           +"\n - 参数:"+JSON.toJSONString(args)
                           + (isDeadlock ? "\n - 死锁： 当前执行出现了死锁" : "")
                           +"\n - error:"+t.toString()+"\n"+t.getMessage()
                           +"\n - 问题处理人:",
                   false);
       }
    });
}

```

## 自动流控规则与实现
> SentinelRuleProcessor.java

 - 开启自动流控 isAutoRule = true
 - 自动流控：当处理线程大于最大线程池95%时，该运行方法将只有50%线程数的线程可以运行该方法。具体查看哨兵的FlowRule线程控制
 - 自动降级: 当方法执行时间>3s时，通过响应时间3s，阻塞2s的窗口时间。

 - 是否自动打断执行时间超过10s的线程,打断后加入黑名单三分钟。isAutoInterrupt = true

