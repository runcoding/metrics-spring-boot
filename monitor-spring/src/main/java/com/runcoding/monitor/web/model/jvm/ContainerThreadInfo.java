package com.runcoding.monitor.web.model.jvm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

/**
 * Created by xukai on 2017/7/27.
 * 当前服务线程信息
 */
public class ContainerThreadInfo {

   /**仍活动的线程总数*/
   private long threadCount;

   /**峰值*/
   private long peakThreadCount;

   /**线程总数（被创建并执行过的线程总数）*/
   private long  totalStartedThreadCount;

   /**当初仍活动的守护线程（daemonThread）总数*/
   private long daemonThreadCount;

   /**当前容器，线程分组后的信息*/
   private Map<String,List<GroupThreadInfo>> threadGroupInfo;

    public long getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(long threadCount) {
        this.threadCount = threadCount;
    }

    public long getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(long peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public void setTotalStartedThreadCount(long totalStartedThreadCount) {
        this.totalStartedThreadCount = totalStartedThreadCount;
    }

    public Map<String, List<GroupThreadInfo>> getThreadGroupInfo() {
        return threadGroupInfo;
    }

    public void setThreadGroupInfo(Map<String, List<GroupThreadInfo>> threadGroupInfo) {
        this.threadGroupInfo = threadGroupInfo;
    }

    public long getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(long daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
}
