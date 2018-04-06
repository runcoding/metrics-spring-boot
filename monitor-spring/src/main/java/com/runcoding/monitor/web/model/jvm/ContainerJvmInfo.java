package com.runcoding.monitor.web.model.jvm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.codahale.metrics.MetricRegistry;
import com.runcoding.monitor.support.metric.MetricProcessor;

import java.util.List;

/**
 * Created by xukai on 2017/7/27.
 * @desc 当前容器jvm信息
 */
public class ContainerJvmInfo {

    /**当前操作系统信息*/
    private OperatingSystemInfo  operatingSystemInfo;

    /**当前服务线程信息*/
    private ContainerThreadInfo   containerThreadInfo;

    /**内存信息*/
    private List<MemoryInfo> containerMemoryInfo;

    /**cpu信息*/
    private  List<ContainerCpuInfo> containerCpuInfos;

    /**接口请求信息*/
    private MetricRegistry metricRegistryInfo = MetricProcessor.getMetricRegistry();

    public OperatingSystemInfo getOperatingSystemInfo() {
        return operatingSystemInfo;
    }

    public void setOperatingSystemInfo(OperatingSystemInfo operatingSystemInfo) {
        this.operatingSystemInfo = operatingSystemInfo;
    }

    public ContainerThreadInfo getContainerThreadInfo() {
        return containerThreadInfo;
    }

    public void setContainerThreadInfo(ContainerThreadInfo containerThreadInfo) {
        this.containerThreadInfo = containerThreadInfo;
    }

    public List<MemoryInfo> getContainerMemoryInfo() {
        return containerMemoryInfo;
    }

    public void setContainerMemoryInfo(List<MemoryInfo> containerMemoryInfo) {

        this.containerMemoryInfo = containerMemoryInfo;
    }

    public MetricRegistry getMetricRegistryInfo() {
        return metricRegistryInfo;
    }

    public void setMetricRegistryInfo(MetricRegistry metricRegistryInfo) {
        this.metricRegistryInfo = metricRegistryInfo;
    }

    public List<ContainerCpuInfo> getContainerCpuInfos() {
        return containerCpuInfos;
    }

    public void setContainerCpuInfos(List<ContainerCpuInfo> containerCpuInfos) {
        this.containerCpuInfos = containerCpuInfos;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
}
