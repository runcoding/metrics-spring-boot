package com.runcoding.monitor.web.model.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by runcoding on 2017/7/27.
 * container 获取当前操作系统信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatingSystemInfo {

    /**运行用户名字*/
    private String runName;

    /**运行的pid*/
    private String runPid;

    /**系统名称*/
    private    String  name;

    /**系统版本*/
    private  String version ;

    /**操作系统的架构*/
    private  String arch;

    /**当前系统内网ip*/
    private String ip;

    /**可用的内核数*/
    private long availableProcessors;

    /**
     * 最后一分钟内系统加载平均值。
     * 系统加载平均值是排队到可用处理器的可运行实体数目与可用处理器上可运行实体数目的总和在某一段时间进行平均的结果。
     * 计算加载平均值的方式是特定于操作系统的，但通常是衰减的与时间相关的平均值。
     * */
    private double systemLoadAverage;

    private long totalMemory ;

    private long freeMemory  ;

    private long maxMemory   ;

    private long usedMemory ;

    private long availableMemory ;

    /**总物理内存(M) jdk9 已经需要添加jar才能使用，作用不大就移除了*/
    @Deprecated
    private long totalPhysicalMemory;

    /**已用物理内存(M)*/
    @Deprecated
    private long usedPhysicalMemorySize;

    /**剩余物理内存(M)*/
    @Deprecated
    private long freePhysicalMemory;

    /**总交换空间(M)*/
    @Deprecated
    private long totalSwapSpaceSize;

    /**已用交换空间(M)*/
    @Deprecated
    private long usedSwapSpaceSize;

    /**剩余交换空间(M)*/
    @Deprecated
    private long freeSwapSpaceSize;


}
