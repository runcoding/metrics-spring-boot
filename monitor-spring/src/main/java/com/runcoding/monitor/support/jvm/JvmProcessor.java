package com.runcoding.monitor.support.jvm;

import com.runcoding.monitor.web.model.jvm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xukai on 2017/7/27.
 */
public class JvmProcessor {

    private  static final long MB = 1024 * 1024;

    private static Logger logger = LoggerFactory.getLogger(JvmProcessor.class);


    private static String ip  ;

    /**获取容器中jvm的信息*/
    public static ContainerJvmInfo getContainerJvmInfo(){
        ContainerJvmInfo jvmInfo = new ContainerJvmInfo();
        /**系统信息*/
        jvmInfo.setOperatingSystemInfo(getOperatingSystemInfo());
        /**线程信息*/
        jvmInfo.setContainerThreadInfo(getContainerThreadInfo());
        /**内存信息*/
        jvmInfo.setContainerMemoryInfo(getContainerMemoryInfo());
        return jvmInfo;
    }

    /**内存信息*/
    public static   List<MemoryInfo> getContainerMemoryInfo(){
        List<MemoryInfo> infos =  new ArrayList<>();
        MemoryMXBean memory    = ManagementFactory.getMemoryMXBean();
        MemoryUsage headMemoryUsage = memory.getHeapMemoryUsage();
        /**head堆*/
        MemoryInfo headMemory = new MemoryInfo("head堆");

        headMemory.setInit(headMemoryUsage.getInit()/MB);
        headMemory.setMax(headMemoryUsage.getMax()/MB);
        headMemory.setUsed(headMemoryUsage.getUsed()/MB);
        headMemory.setCommitted(headMemoryUsage.getCommitted()/MB);
        headMemory.setUsedRate(headMemoryUsage.getUsed()*100/headMemoryUsage.getCommitted());
        infos.add(headMemory);

        /**non-head非堆*/
        MemoryInfo nonHeadMemory = new MemoryInfo("non-head堆");
        MemoryUsage nonheadMemory = memory.getNonHeapMemoryUsage();
        nonHeadMemory.setInit(nonheadMemory.getInit()/MB);
        nonHeadMemory.setMax(nonheadMemory.getMax()/MB);
        nonHeadMemory.setUsed(nonheadMemory.getUsed()/MB);
        nonHeadMemory.setCommitted(nonheadMemory.getCommitted()/MB);
        nonHeadMemory.setUsedRate(nonheadMemory.getUsed()*100/nonheadMemory.getCommitted());
        infos.add(nonHeadMemory);
        /**vm各内存区信息*/
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        if(pools == null ){
            return infos;
        }
        for(MemoryPoolMXBean pool : pools){
            /**只打印一些各个内存区都有的属性，一些区的特殊属性，可看文档 */
            MemoryInfo poolMemory = new MemoryInfo(pool.getName());
            MemoryUsage usage = pool.getUsage();
            poolMemory.setInit(usage.getInit()/MB);
            poolMemory.setMax(usage.getMax()/MB);
            poolMemory.setUsed(usage.getUsed()/MB);
            poolMemory.setCommitted(usage.getCommitted()/MB);
            poolMemory.setUsedRate(usage.getUsed()*100/usage.getCommitted());
            infos.add(poolMemory);
        }
        return infos;
    }

    /**线程信息*/
    public static ContainerThreadInfo getContainerThreadInfo(){
        ContainerThreadInfo info =  new ContainerThreadInfo();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        info.setThreadCount(threadMXBean.getThreadCount());
        info.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        info.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        info.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());

        ThreadInfo[] threads = threadMXBean.dumpAllThreads(false, false);
        Map<String, List<GroupThreadInfo>> threadGroupInfo = new HashMap<>();
        for (ThreadInfo  t : threads) {
            String threadName = t.getThreadName();
            String[] split    = threadName.split("-");
            String groupName  = split[0];
            if(groupName.length() == threadName.length()){
                groupName  = threadName.split("_")[0];
            }
            if(groupName.length() == threadName.length()){
                groupName  = threadName.split("#")[0];
            }
            if(groupName.length() == threadName.length()){
                groupName  = threadName.split(" ")[0];
            }
            List<GroupThreadInfo> groupNames = threadGroupInfo.get(groupName);
            if(groupNames == null){
                groupNames  = new ArrayList<>();
            }
            groupNames.add(new GroupThreadInfo(t.getThreadName(),t.getThreadId(),t.getThreadState()));
            threadGroupInfo.put(groupName,groupNames);
        }
        info.setThreadGroupInfo(threadGroupInfo);
        return info;
    }


    /**系统信息*/
    public static OperatingSystemInfo getOperatingSystemInfo(){

        OperatingSystemInfo systemInfo = new OperatingSystemInfo();

        OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();

        systemInfo.setName(system.getName());
        systemInfo.setVersion(system.getVersion());
        systemInfo.setArch(system.getArch());
        systemInfo.setAvailableProcessors(system.getAvailableProcessors());
        systemInfo.setIp(getIp());

        long totalPhysicalMemory = getLongFromOperatingSystem(system,"getTotalPhysicalMemorySize");
        long freePhysicalMemory = getLongFromOperatingSystem(system, "getFreePhysicalMemorySize");
        long usedPhysicalMemorySize =totalPhysicalMemory - freePhysicalMemory;

        systemInfo.setTotalPhysicalMemory(totalPhysicalMemory/MB);
        systemInfo.setUsedPhysicalMemorySize(usedPhysicalMemorySize/MB);
        systemInfo.setFreePhysicalMemory(freePhysicalMemory/MB);

        long  totalSwapSpaceSize = getLongFromOperatingSystem(system, "getTotalSwapSpaceSize");
        long freeSwapSpaceSize = getLongFromOperatingSystem(system, "getFreeSwapSpaceSize");
        long usedSwapSpaceSize = totalSwapSpaceSize - freeSwapSpaceSize;

        systemInfo.setTotalSwapSpaceSize(totalSwapSpaceSize/MB);
        systemInfo.setUsedSwapSpaceSize(usedSwapSpaceSize/MB);
        systemInfo.setFreeSwapSpaceSize(freeSwapSpaceSize/MB);

        return systemInfo;
    }

    /**获取ip*/
    public static String getIp() {
        try {
            if(ip == null){
                ip = InetAddress.getLocalHost().getHostAddress().toString();
            }
            return ip;
        } catch (UnknownHostException e) {
            return   "127.0.0.1";
        }
    }

    private static long getLongFromOperatingSystem(OperatingSystemMXBean operatingSystem, String methodName) {
        try {
            final Method method = operatingSystem.getClass().getMethod(methodName,
                    (Class<?>[]) null);
            method.setAccessible(true);
            return (Long) method.invoke(operatingSystem, (Object[]) null);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalStateException(e.getCause());
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
