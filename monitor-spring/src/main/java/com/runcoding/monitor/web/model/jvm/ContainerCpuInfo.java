package com.runcoding.monitor.web.model.jvm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by xukai on 2017/7/28.
 * @desc 当前服务器cpu
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
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

}
