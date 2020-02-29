package com.runcoding.monitor.support.monitor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.exception.SentinelBlockException;
import com.runcoding.monitor.support.monitor.sentinel.SentinelRuleProcessor;
import com.runcoding.monitor.support.webhook.dingtalk.DTWebHookProcessor;
import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.support.monitor.function.MonitorSendFunction;
import com.runcoding.monitor.support.monitor.sentinel.SentinelProcessor;
import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.utils.ErrorMessageUtils;
import com.runcoding.monitor.web.utils.IpUtils;
import com.runcoding.monitor.web.utils.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

/**
 *  系统服务监控
 * @author runcoding
 * @date 2018-03-21
 * @desc 统计接口tps
 */
@Aspect
@Component
public class MonitorProcessor {

    private Logger logger = LoggerFactory.getLogger(MonitorProcessor.class);

    private MonitorSendFunction monitorSendFunction;

    /**切入Controller*/
    @Pointcut("bean(*Controller)")
    public void controllerMethodPointcut() {}

    /**切入Service*/
    @Pointcut("bean(*Service)")
    public void servicesMethodPointcut(){}

    /**环绕Controller的方法*/
    @Around("controllerMethodPointcut() ")
    public Object controllerProcessor(ProceedingJoinPoint joinPoint) throws Throwable {
        return processor(joinPoint,true);
    }

    /**环绕Service的方法*/
    @Around("servicesMethodPointcut() ")
    public Object servicesProcessor(ProceedingJoinPoint joinPoint) throws Throwable {
        return processor(joinPoint,false);
    }

    /**环绕的方法*/
    public Object processor(ProceedingJoinPoint joinPoint,boolean isController) throws Throwable {
        Method method                 = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object[] args                 = joinPoint.getArgs();
        String className              = method.getDeclaringClass().getSimpleName();
        String methodName             = methodName(method);
        /**执行方法是不是在黑名单中,黑名单方法直接拒绝执行*/
        boolean isBlockMethod         = isBlockMethod(className, methodName);
        if(isBlockMethod){
            throw new SentinelBlockException(Resp.failure("服务正忙，请稍后重试……"));
        }
        /**执行方法是不是在白名单中*/
        boolean isWhiteMethod         = isWhiteMethod(className, methodName);
        long startCurrentTimeMillis   = System.currentTimeMillis();
        /**当前执行线程最先执行controller还是service。只拦截一次*/
        boolean isThreadRunningMethod = MetricProcessor.threadRunningMethod(methodName,startCurrentTimeMillis, isController);
        /**高负载流控*/
        SentinelProcessor.highLoadSentinelAutoRule(isWhiteMethod,isThreadRunningMethod,methodName);
        Entry entry = null;
        try {
            /**获取许可*/
            if(!isWhiteMethod && isThreadRunningMethod){
                 entry = SphU.entry(methodName, EntryType.IN);
             }
             return joinPoint.proceed();
        } catch (Throwable t) {
            if (BlockException.isBlockException(t)){
                String ruleLimitApp =t.getMessage();
                if(t instanceof  BlockException){
                   ruleLimitApp = ((BlockException) t).getRuleLimitApp();
                }
                logger.warn("执行{}方法，被拒绝(限流)执行。拒绝类型：ruleLimitApp={}",methodName,ruleLimitApp);
                throw new SentinelBlockException(Resp.failure("服务正忙，请稍后重试……"));
            }
            /**发送报警*/
            chatBotSendLog(methodName,t,args);
            Tracer.trace(t);
            throw t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
            /**执行耗时ms*/
            long duration = (System.currentTimeMillis() - startCurrentTimeMillis);
            /**自动限流规则(执行时间大于等于3s)*/
            SentinelProcessor.timeOutSentinelAutoRule(isWhiteMethod,isThreadRunningMethod,methodName,args,duration);
            if(isThreadRunningMethod){
                /**销毁数据*/
                SentinelProcessor.destroy();
                MetricProcessor.destroy();
            }
        }
    }

    private String methodName(Method method){
        /**执行类名称*/
        StringBuilder sb = new StringBuilder(method.getDeclaringClass().getSimpleName());
        sb.append(".");
        /**执行方法名称*/
        sb.append(method.getName());
        sb.append('(');
        sb.append(method.getParameterTypes().length);
        sb.append(')');
        return sb.toString();
    }



    private void  chatBotSendLog(String methodName , Throwable t, Object[] args){
        if(t instanceof  Error){
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName + t.getClass().getSimpleName());
            if(!checkEnableSend){
                return;
            }
            /**系统级异常*/
            DTWebHookProcessor.chatbotSendByMarkdown("系统级高风险报警", "服务出现系统ERROR,请即时处理 " + getErrMessage(methodName, t, args), true);
            /**虚拟机运行异常(内存溢出)*/
            if(t instanceof VirtualMachineError){
                logger.error("虚拟机异常(内存溢出),当前正在执行的方法有:{}",MetricProcessor.methodThreadRunningCnt());
            }
        } else if(t instanceof SQLException ){
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName + t.getClass().getSimpleName());
            if(!checkEnableSend){
                return;
            }
            /**SQL异常*/
            DTWebHookProcessor.chatbotSendByMarkdown("SQL报警", "请即时排查sql异常 " +getErrMessage(methodName, t, args), true);
        } else if(t instanceof InterruptedException){
            Thread thread = Thread.currentThread();
            logger.warn("打断threadName={},threadId={}，执行的{}服务:",thread.getName(),thread.getId(),methodName);
            boolean checkEnableSend = DTWebHookProcessor.checkEnableSend(methodName + t.getClass().getSimpleName());
            if(!checkEnableSend){
                return;
            }
            /**执行线程被打断*/
            DTWebHookProcessor.chatbotSendByMarkdown("执行线程被打断", "当前执行中的方法因超时被打断,请即时处理。 " + getErrMessage(methodName, t, args) , true);
        }

        if(monitorSendFunction != null){
           monitorSendFunction.chatBotSendLog(methodName,t,args);
        }
    }

    private String getErrMessage(String methodName, Throwable t, Object[] args) {
        return "\n - 服务:" + MonitorConstants.applicationName + "-" + MonitorConstants.applicationEnv
                + "\n - ip:" + IpUtils.getIp() + ":" + MonitorConstants.applicationPort
                + "\n - 方法:" + methodName
                + "\n - 参数:" + MethodUtils.methodArgs(args)
                + "\n - error:" + ErrorMessageUtils.getMessage(t);
    }

    /**是否白名单方法*/
    private boolean isWhiteMethod(String className,String classMethodName){
        Map<String, Long> authorityWhite = SentinelRuleProcessor.getAuthorityWhite();
        if(authorityWhite.containsKey(className)){
            return true;
        }
        return authorityWhite.containsKey(classMethodName);
    }

    /**是否黑名单方法*/
    private boolean isBlockMethod(String className,String classMethodName){
        long currentTimeMillis = System.currentTimeMillis();
        Map<String, Long> authorityBlock = SentinelRuleProcessor.getAuthorityBlock();
        /**类过滤*/
        Long timeout = authorityBlock.get(className);
        if(timeout != null){
            if(timeout == -1 || timeout > currentTimeMillis){
               return true;
            }
            authorityBlock.remove(className);
        }
        /**方法过滤*/
        timeout = authorityBlock.get(classMethodName);
        if(timeout != null ){
            if(timeout == -1 || timeout > currentTimeMillis){
                return true;
            }
            authorityBlock.remove(classMethodName);
        }
        return false;
    }

    public  void setMonitorSendFunction(MonitorSendFunction monitorSendFunction) {
        this.monitorSendFunction = monitorSendFunction;
    }

}