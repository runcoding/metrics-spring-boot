package com.runcoding.monitor.web.model.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author  xukai
 * @time 2017/10/26.
 */
public class MetricInfo {

    private Integer id;

    /**
     * 统计日期(yyyy-mm-dd)
     */
    private String refDate;

    /**本次记录名称*/
    private String  name;

    /**本次记录次数*/
    private long count;

    /**本次记录分钟均值*/
    private double minuteRate;

    public MetricInfo() {
    }

    public MetricInfo(String refDate, String name, long count, double minuteRate) {
        this.refDate = refDate;
        this.name = name;
        this.count = count;
        this.minuteRate = minuteRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMinuteRate() {
        return minuteRate;
    }

    public void setMinuteRate(double minuteRate) {
        this.minuteRate = minuteRate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
}
