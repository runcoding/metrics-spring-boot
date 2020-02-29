package com.runcoding.monitor.web.model;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/31 17:10
 * @describe: 监控系统系统常量
 **/
public class MonitorConstants {

    /**应用名称*/
    public static String applicationName;

    /**应用环境*/
    public static String applicationEnv;

    /**应用运行的端口*/
    public static String applicationPort;

    /**
     * tomcat最大处理http请求数
     * org.apache.tomcat.util.threads.ThreadPoolExecutor
     * */
    public static long serverMaxThreads = 200;

    /**最大的负载比例(当前正在运行的线程/容器最大数量)，这里的容器默认指tomcat*/
    public static  double  maxLoadAverageRate = 0.95d;

    /**是否开启自动限流(默认开启，超过1s)*/
    public static boolean isAutoRule = true;

    /**调用请求超时3s,输出服务当前运行日志*/
    public static long warnTimeoutMillis = 3000L;

    /**是否自动打断执行线程*/
    public static boolean isAutoInterrupt   = true;

    /**最长的方法执行时长(ms,默认10s)*/
    public static long maxRunTimeoutMillis = 10000L;

    /**加入黑名单时长(ms,默认3分钟)*/
    public static long maxBlockTimeMillis = 180000L;

    /** 超过这个时间(ms,默认0.5s)的请求,将被统计*/
    public  static Long maxTardinessMillis = 500L;

    /**1s = 1000ms */
    public  static Long millisecond = 1000L;

    /**当前节点是否正在刷新*/
    public static volatile boolean isRefresh = false;


}
