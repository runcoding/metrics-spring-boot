package com.runcoding.monitor.web.job;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.support.metric.SentinelMetricProcessor;
import com.runcoding.monitor.web.dao.MetricInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2019/07/29 10:47
 * @description 服务监控数据job
 * Copyright (C), 2017-2018, runcoding
 **/
@Component
public class MonitorJob {

    private int circleSize = 6;

    private Logger logger = LoggerFactory.getLogger(MonitorJob.class);

    @Autowired
    private MetricInfoMapper metricInfoMapper;

    /**每分钟监控 上一次执行完毕时间点之后30秒再执行*/
    @Scheduled(fixedDelay = 30000)
    public void  monitorMinute(){
        MetricProcessor.interruptRunningTimeout();
    }

    /**获取近一小时的统计数据,上一次执行完毕时间点之后1小时再执行*/
    @Scheduled(fixedDelay = 3600000)
    public void  monitorHour(){
        try{
            long currentTimeMillis = System.currentTimeMillis();
            String refDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Map<String, MethodMetricInfo> metricInfoMap = new HashMap<>(64);
            for (int i = 0; i < circleSize; i++) {
                CommandRequest request = new CommandRequest();
                request.addParam("endTime",String.valueOf(currentTimeMillis-1000));
                 /**十分以内的数据*/
                currentTimeMillis -= 600000;
                request.addParam("startTime",String.valueOf(currentTimeMillis));
                List<MethodMetricInfo> metricInfoList = SentinelMetricProcessor.getMethodMetricInfo(request);

                Map<String, MethodMetricInfo> tmpMetricMap = metricInfoList.stream().
                        collect(Collectors.toMap(m -> m.getName(), m -> m, this::collectMethodMetricInfo));
                if(metricInfoMap.isEmpty()){
                    metricInfoMap.putAll(tmpMetricMap);
                    continue;
                }
                tmpMetricMap.forEach((methodName,newMethodMetricInfo)-> {
                    MethodMetricInfo methodMetricInfo = metricInfoMap.get(methodName);
                    if(methodMetricInfo == null){
                        metricInfoMap.put(methodName,newMethodMetricInfo);
                        return;
                    }
                    collectMethodMetricInfo(methodMetricInfo,newMethodMetricInfo);
                });
            }
            metricInfoMap.forEach((methodName,methodMetricInfo)-> {
                /**最长执行时长(ms)*/
                Long tardiness = MetricProcessor.removeTardinessMethod(methodName);
                methodMetricInfo.setTardiness(tardiness == null ? 0L : tardiness);
                methodMetricInfo.setRefDate(refDate);
                metricInfoMapper.insert(methodMetricInfo);
            });
            /**清空7天之前的数据*/
            int deleteNum = metricInfoMapper.delete(LocalDateTime.now().plusDays(-7).
                            format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if(deleteNum>0){
                /**释放磁盘空间*/
                metricInfoMapper.vacuum();
            }
        }catch (Exception e){
            logger.warn("每小时统计一次服务监控出错",e);
        }

    }

     /**聚合多个结果*/
     public MethodMetricInfo collectMethodMetricInfo(MethodMetricInfo oldVal, MethodMetricInfo newVal){
        if(newVal == null){
            return oldVal;
        }
        oldVal.setCntPassRequest(oldVal.getCntPassRequest() + newVal.getCntPassRequest());
        oldVal.setCntBlockRequest(oldVal.getCntBlockRequest() + newVal.getCntBlockRequest());
        oldVal.setCntRequest(oldVal.getCntRequest() + newVal.getCntRequest());
        oldVal.setCntSuccessRequest(oldVal.getCntSuccessRequest() + newVal.getCntSuccessRequest());
        oldVal.setCntExceptionRequest(oldVal.getCntExceptionRequest() + newVal.getCntExceptionRequest());
        long oldValAvgRt = oldVal.getAvgRt();
        long newValAvgRt = newVal.getAvgRt();
        oldVal.setAvgRt(oldValAvgRt > newValAvgRt ? oldValAvgRt :  newValAvgRt);
        return oldVal;
    };

}
