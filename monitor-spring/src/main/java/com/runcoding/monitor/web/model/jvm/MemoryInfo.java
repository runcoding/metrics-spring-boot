package com.runcoding.monitor.web.model.jvm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created by xukai on 2017/7/27.
 * 内存信息
 */
public class MemoryInfo {

    private String name;

    /**初始(M)*/
    private long init;

    /**最大(上限)(M)*/
    private long max;

    /**当前(已使用)(M)*/
    private long used;

    /**提交的内存(已申请)(M)*/
    private long committed;

    /**使用率(%)*/
    private long usedRate;

    public MemoryInfo() {
    }

    public MemoryInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInit() {
        return init;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getCommitted() {
        return committed;
    }

    public void setCommitted(long committed) {
        this.committed = committed;
    }

    public long getUsedRate() {
        return usedRate;
    }

    public void setUsedRate(long usedRate) {
        this.usedRate = usedRate;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
}
