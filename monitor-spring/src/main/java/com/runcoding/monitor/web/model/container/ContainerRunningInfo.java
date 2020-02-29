package com.runcoding.monitor.web.model.container;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.runcoding.monitor.support.jvm.JvmProcessor;
import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.support.metric.SentinelMetricProcessor;
import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by runcoding on 2017/7/27.
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

    /**当前方法被多少个线程正在执行*/
    private Map<String,Integer>  methodThreadRunningCnt;

    /**方法执行指标信息*/
    private ContainerMethodMonitorInfo methodMetricInfo;

    /**GC 运行次数*/
    private List<GarbageCollectorInfo> garbageCollectorInfoList;

    /**构建容器运行信息*/
    public static ContainerRunningInfo buildContainerRunningInfo(boolean isDumpAllThread , String proposalMethodName){
        ContainerRunningInfo runningInfo = new ContainerRunningInfo();
        runningInfo.setOperatingSystemInfo(JvmProcessor.getOperatingSystemInfo());
        runningInfo.setContainerMemoryInfo(JvmProcessor.getContainerMemoryInfo());
        /**获取限制的线程信息(获取堆栈信息相当于jstack,注意会影响服务运行的性能)*/
        runningInfo.setContainerThreadInfo(JvmProcessor.getContainerThreadInfo(isDumpAllThread));
        runningInfo.setMethodThreadRunningCnt(MetricProcessor.methodThreadRunningCnt());

        /**获取一分钟的数据*/
        CommandRequest request = new CommandRequest();
        long currentTimeMillis = System.currentTimeMillis();
        request.addParam("startTime",String.valueOf(currentTimeMillis-60000));
        List<MethodMetricInfo> metricInfoList = SentinelMetricProcessor.getMethodMetricInfo(request);
        ContainerMethodMonitorInfo methodMetricInfo =  new ContainerMethodMonitorInfo();
        runningInfo.setMethodMetricInfo(methodMetricInfo);
        if(CollectionUtils.isEmpty(metricInfoList)){
            return  runningInfo;
        }
        Comparator<MethodMetricInfo> byReducePassRequest = Comparator.comparing(m ->m.getCntPassRequest());
        Set<String> methodNames = new HashSet<>();
        String maxPassRequestMethodName = metricInfoList.stream()
                .filter(f ->
                /**忽略掉sentinel监控信息*/
                !org.apache.commons.lang3.StringUtils.containsAny(f.getName(),
                        Constants.TOTAL_IN_RESOURCE_NAME,
                        Constants.CPU_USAGE_RESOURCE_NAME,
                        Constants.SYSTEM_LOAD_RESOURCE_NAME
                )).map(m -> {methodNames.add(m.getName());return m; })
                .sorted(byReducePassRequest.reversed()).findFirst().get().getName();

        if(StringUtils.isEmpty(proposalMethodName)){
            proposalMethodName = maxPassRequestMethodName ;
        }

        String finalProposalMethodName = proposalMethodName;
        List<MethodMetricInfo> proposalMetricList = metricInfoList.stream().
                filter(m -> StringUtils.endsWithIgnoreCase(m.getName(), finalProposalMethodName)).collect(Collectors.toList());

        methodMetricInfo.setMethodNames(methodNames);
        methodMetricInfo.setProposalMethodName(proposalMethodName);
        methodMetricInfo.setProposalMetricList(proposalMetricList);

        runningInfo.setMethodMetricInfo(methodMetricInfo);

        /**gc 信息*/
        List<GarbageCollectorInfo> gcList = ManagementFactory.getGarbageCollectorMXBeans().stream().map(gc ->
                GarbageCollectorInfo.builder().gcName(gc.getName()).gcCount(gc.getCollectionCount()).gcTime(gc.getCollectionTime()).build()
        ).collect(Collectors.toList());
        runningInfo.setGarbageCollectorInfoList(gcList);
        return runningInfo;
    }
}
