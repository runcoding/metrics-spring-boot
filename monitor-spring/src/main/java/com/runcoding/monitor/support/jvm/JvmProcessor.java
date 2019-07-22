package com.runcoding.monitor.support.jvm;


import com.runcoding.monitor.web.model.jvm.ContainerThreadInfo;
import com.runcoding.monitor.web.model.jvm.GroupThreadInfo;
import com.runcoding.monitor.web.model.jvm.MemoryInfo;
import com.runcoding.monitor.web.model.jvm.OperatingSystemInfo;
import org.assertj.core.util.Lists;

import java.lang.management.*;
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

    /**内存信息*/
    public static   List<MemoryInfo> getContainerMemoryInfo(){
        List<MemoryInfo> memoryInfoList =  new ArrayList<>();
        MemoryMXBean memory    = ManagementFactory.getMemoryMXBean();
        /**堆内存*/
        MemoryInfo headMemory = memoryInfoBuild("堆内存",memory.getHeapMemoryUsage());

        memoryInfoList.add(headMemory);

        /**堆外内存*/
        MemoryInfo nonHeadMemory =  memoryInfoBuild("堆外内存",memory.getNonHeapMemoryUsage());
        memoryInfoList.add(nonHeadMemory);
        /**vm各内存区信息*/
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        if(pools == null ){
            return memoryInfoList;
        }
        for(MemoryPoolMXBean pool : pools){
            MemoryUsage usage = pool.getUsage();
            /**只打印一些各个内存区都有的属性，一些区的特殊属性，可看文档 */
            MemoryInfo poolMemory = MemoryInfo.builder()
                    .name(pool.getName())
                    .init(usage.getInit()/MB)
                    .max(usage.getMax()/MB)
                    .used(usage.getUsed()/MB)
                    .committed(usage.getCommitted()/MB)
                    .usedRate(usage.getUsed()*100/usage.getCommitted())
                    .build();
            memoryInfoList.add(poolMemory);
        }
        return memoryInfoList;
    }

    private static MemoryInfo memoryInfoBuild(String name ,MemoryUsage headMemoryUsage) {
        return MemoryInfo.builder()
                .name(name)
                .init(headMemoryUsage.getInit()/MB)
                .max(headMemoryUsage.getMax()/MB)
                .used(headMemoryUsage.getUsed()/MB)
                .committed(headMemoryUsage.getCommitted()/MB)
                .usedRate(headMemoryUsage.getUsed()*100/headMemoryUsage.getCommitted())
                .build();
    }

    /**线程信息*/
    public static ContainerThreadInfo getContainerThreadInfo(){
        ContainerThreadInfo info =  new ContainerThreadInfo();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        info.setThreadCount(threadMXBean.getThreadCount());
        info.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        info.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        info.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        info.setThreadGroupInfo(getThreadGroupInfo());
        return info;
    }

    public static Map<String, List<GroupThreadInfo>> getThreadGroupInfo(){
        Map<String, List<GroupThreadInfo>> threadGroupInfo = new HashMap<>();
        ThreadGroup root =ThreadUtil.getRoot();
        Thread[] threads = new Thread[root.activeCount()];
        while (root.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        for (Thread thread : threads) {
            if (thread == null) {
                continue;
            }
            String  threadName = thread.getName();
            long      threadId = thread.getId();
            Thread.State state = thread.getState();
            buildThreadInfo(threadGroupInfo, threadName, threadId, state);
        }
        return threadGroupInfo;
    }


    /**获取堆栈信息相当于jstack,注意会影响服务运行的性能*/
    @Deprecated
    public static Map<String, List<GroupThreadInfo>> getThreadGroupInfo(ThreadMXBean threadMXBean,boolean isDumpAllThread) {
        ThreadInfo[] threads = {};
        if(isDumpAllThread){
            threads = threadMXBean.dumpAllThreads(false, false);
        }
        Map<String, List<GroupThreadInfo>> threadGroupInfo = new HashMap<>();
        for (ThreadInfo  t : threads) {
            String    threadName = t.getThreadName();
            long        threadId = t.getThreadId();
            Thread.State   state = t.getThreadState();
            buildThreadInfo(threadGroupInfo, threadName, threadId, state);
        }
        return threadGroupInfo;
    }

    private static void buildThreadInfo(Map<String, List<GroupThreadInfo>> threadGroupInfo,
                                        String threadName, long threadId,
                                        Thread.State threadState) {
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
        List<GroupThreadInfo> groupNames = threadGroupInfo.getOrDefault(groupName, Lists.newArrayList());
        groupNames.add(new GroupThreadInfo(threadName,threadId,threadState));
        threadGroupInfo.put(groupName,groupNames);
    }

    /**当前运行系统名称*/
    public static String getRunName(){
        String runInfo = ManagementFactory.getRuntimeMXBean().getName();
        String[] runInfoName = runInfo.split("@");
        return  runInfoName.length >0 ? runInfoName[1] : "user";
    }

    /**当前运行系统名称*/
    public static String getRunPid(){
        String runInfo = ManagementFactory.getRuntimeMXBean().getName();
        String[] runInfoName = runInfo.split("@");
        return  runInfoName[0];
    }

    /**系统信息*/
    public static OperatingSystemInfo getOperatingSystemInfo(){

        OperatingSystemInfo systemInfo = new OperatingSystemInfo();

        systemInfo.setRunName(getRunName());
        systemInfo.setRunPid(getRunPid());

        OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();

        systemInfo.setName(system.getName());
        systemInfo.setVersion(system.getVersion());
        systemInfo.setArch(system.getArch());
        systemInfo.setAvailableProcessors(system.getAvailableProcessors());

        systemInfo.setIp(getIp());
        systemInfo.setSystemLoadAverage(system.getSystemLoadAverage());

        Runtime runtime = Runtime.getRuntime();

        long totalPhysicalMemory = runtime.totalMemory();
        long freePhysicalMemory  = runtime.freeMemory();
        long usedPhysicalMemorySize =totalPhysicalMemory - freePhysicalMemory;

        systemInfo.setTotalPhysicalMemory(totalPhysicalMemory/MB);
        systemInfo.setUsedPhysicalMemorySize(usedPhysicalMemorySize/MB);
        systemInfo.setFreePhysicalMemory(freePhysicalMemory/MB);


        long totalSwapSpaceSize = 0;
        long freeSwapSpaceSize  = 0;
        long usedSwapSpaceSize  = totalSwapSpaceSize - freeSwapSpaceSize;

        systemInfo.setTotalSwapSpaceSize(totalSwapSpaceSize/MB);
        systemInfo.setUsedSwapSpaceSize(usedSwapSpaceSize/MB);
        systemInfo.setFreeSwapSpaceSize(freeSwapSpaceSize/MB);
        return systemInfo;
    }

    /**获取ip*/
    public static String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return   "127.0.0.1";
        }
    }
}