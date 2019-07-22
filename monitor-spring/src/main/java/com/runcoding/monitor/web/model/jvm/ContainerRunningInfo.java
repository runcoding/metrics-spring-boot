package com.runcoding.monitor.web.model.jvm;

import com.codahale.metrics.MetricRegistry;
import com.runcoding.monitor.support.jvm.JvmProcessor;
import com.runcoding.monitor.support.metric.MetricProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xukai on 2017/7/27.
 * @desc 当前容器jvm信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerRunningInfo {

    /**当前操作系统信息*/
    private OperatingSystemInfo  operatingSystemInfo;

    /**当前服务线程信息*/
    private ContainerThreadInfo   containerThreadInfo;

    /**内存信息*/
    private List<MemoryInfo> containerMemoryInfo;

    /**接口请求信息*/
    private MetricRegistry metricRegistryInfo = MetricProcessor.getMetricRegistry();
    


    /**构建容器运行信息*/
    public static ContainerRunningInfo buildContainerRunningInfo(){
        ContainerRunningInfo runningInfo = new ContainerRunningInfo();
        runningInfo.setOperatingSystemInfo(JvmProcessor.getOperatingSystemInfo());
        runningInfo.setContainerMemoryInfo(JvmProcessor.getContainerMemoryInfo());
        /**获取限制的线程信息(获取堆栈信息相当于jstack,注意会影响服务运行的性能)*/
        runningInfo.setContainerThreadInfo(JvmProcessor.getContainerThreadInfo());
        return runningInfo;
    }
}
