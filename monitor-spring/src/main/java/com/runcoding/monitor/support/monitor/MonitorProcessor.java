package com.runcoding.monitor.support.monitor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.runcoding.monitor.support.jvm.JvmProcessor;
import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.web.model.jvm.ContainerThreadInfo;
import com.runcoding.monitor.web.model.jvm.MemoryInfo;
import com.runcoding.monitor.web.model.jvm.OperatingSystemInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 *  系统服务监控
 * @author xukai
 * @date 2018-03-21
 * @desc 统计接口tps
 */
@Aspect
@Component
public class MonitorProcessor {

    private Logger logger = LoggerFactory.getLogger(MonitorProcessor.class);

    private MetricRegistry metricRegistry = MetricProcessor.getMetricRegistry();

    /**接口一分钟平均值调用请求超时3s,输出服务当前运行日志*/
    @Value("${runcoding.monitor.warn_time_out:3000000000}")
    private long Warn_Time_Out;

    /**切入Service*/
    @Pointcut("bean(*Service)")
    public void servicesMethodPointcut(){}

    /**api 接口切点*/
    @Pointcut("bean(*Controller)")
    public void requestMethodPointcut() { }


    /**环绕的方法*/
    @Around("servicesMethodPointcut() || requestMethodPointcut() ")
    public Object redisCache(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        StringBuffer sb = new StringBuffer(method.getName());
        sb.append('(');
        sb.append(method.getParameterTypes().length);
        sb.append(')');

        String className = method.getDeclaringClass().getSimpleName();

        Timer timer = metricRegistry.timer(MetricRegistry.name(className,sb.toString()));
        Timer.Context  ctx = timer.time();

        try {
            return joinPoint.proceed();
        } finally {
            /**计时结束*/
            long elapsed = ctx.stop();
            toLogger(method,timer,elapsed);
        }
    }

    /***打印日志*/
    private void toLogger(Method method,  Timer timer,long elapsed) {

        if(elapsed< Warn_Time_Out){
            return;
        }
        /**执行类名称*/
        String className = method.getDeclaringClass().getSimpleName();
        /**执行方法名称*/
        String methodName = method.getName();
        OperatingSystemInfo systemInfo = JvmProcessor.getOperatingSystemInfo();
        /** 系统总内存*/
        long totalPhysicalMemory = systemInfo.getTotalPhysicalMemory();
        /** 系统总剩余内存*/
        long freePhysicalMemory = systemInfo.getFreePhysicalMemory();
        MemoryInfo headMemory = JvmProcessor.getContainerMemoryInfo().get(0);
        /** 堆内分配内存*/
        long headMemoryMax = headMemory.getMax();
        /** 堆内已使用内存*/
        long headMemoryUsed = headMemory.getUsed();

        logger.warn("【执行】[method]{}.{},近一分钟tps：{},耗时均值:{},当前处理时长:{},[系统]总内存:{},剩余内存:{},[堆]总内存:{},已使用:{}"
                , className, methodName, timer.getOneMinuteRate(),timer.getSnapshot().getMean(),elapsed/1000000000,totalPhysicalMemory,
                freePhysicalMemory, headMemoryMax, headMemoryUsed);
    }




}