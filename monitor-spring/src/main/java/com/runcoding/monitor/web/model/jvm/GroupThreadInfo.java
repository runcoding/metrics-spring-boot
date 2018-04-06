package com.runcoding.monitor.web.model.jvm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created by xukai on 2017/7/27.
 * 线程信息
 */
public class GroupThreadInfo {

    /**线程名称*/
    private String threadName;

    /**线程id*/
    private long   threadId;

    /**线程的状态*/
    private  Thread.State threadState;

    public GroupThreadInfo() {
    }

    public GroupThreadInfo(String threadName, long threadId, Thread.State threadState) {
        this.threadName = threadName;
        this.threadId = threadId;
        this.threadState = threadState;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public Thread.State getThreadState() {
        return threadState;
    }

    public void setThreadState(Thread.State threadState) {
        this.threadState = threadState;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
}
