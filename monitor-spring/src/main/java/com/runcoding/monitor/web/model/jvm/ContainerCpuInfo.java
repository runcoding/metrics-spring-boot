package com.runcoding.monitor.web.model.jvm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created by xukai on 2017/7/28.
 * @desc 当前服务器cpu
 */
public class ContainerCpuInfo {

    /**多核cpu时，编号*/
    private int cpuNum;

    /**CPU的总量MHz*/
    private int mhz ;

    /**获得CPU的卖主，如：Intel*/
    private String vendor ;

    /**获得CPU的类别，如：Celeron*/
    private String model ;

    /**缓冲存储器数量*/
    private  long cacheSize;

    /**CPU用户使用率*/
    private double user;

    /**系统使用率*/
    private double sys;

    /**当前空闲率*/
    private double idle;

    /**当前等待率*/
    private double wait;

    /***/
    private double stolen;

    /**总的使用率*/
    private double combined;

    public int getCpuNum() {
        return cpuNum;
    }

    public void setCpuNum(int cpuNum) {
        this.cpuNum = cpuNum;
    }

    public int getMhz() {
        return mhz;
    }

    public void setMhz(int mhz) {
        this.mhz = mhz;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public double getUser() {
        return user;
    }

    public void setUser(double user) {
        this.user = user;
    }

    public double getSys() {
        return sys;
    }

    public void setSys(double sys) {
        this.sys = sys;
    }

    public double getIdle() {
        return idle;
    }

    public void setIdle(double idle) {
        this.idle = idle;
    }

    public double getWait() {
        return wait;
    }

    public void setWait(double wait) {
        this.wait = wait;
    }

    public double getStolen() {
        return stolen;
    }

    public void setStolen(double stolen) {
        this.stolen = stolen;
    }

    public double getCombined() {
        return combined;
    }

    public void setCombined(double combined) {
        this.combined = combined;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
}
