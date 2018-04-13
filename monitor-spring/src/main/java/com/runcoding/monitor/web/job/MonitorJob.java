package com.runcoding.monitor.web.job;

import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.web.dao.MetricInfoMapper;
import com.runcoding.monitor.web.model.metrics.MetricInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author: xukai
 * @email: runcoding@163.com
 * @created Time: 2018/3/29 10:47
 * @description 服务监控数据job
 **/
@Component
public class MonitorJob {

    private Logger logger = LoggerFactory.getLogger(MonitorJob.class);

    @Autowired
    private MetricInfoMapper metricInfoMapper;


    @Scheduled(cron = "0 0 * * * ?")
    public void  execute(){
        try{
            List<MetricInfo> metricInfos = MetricProcessor.clearAll();
            metricInfos.forEach(metricInfo -> {
                metricInfoMapper.insert(metricInfo);
            });
            /**清空7天之前的数据*/
            metricInfoMapper.delete(LocalDateTime.now().plusDays(-7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }catch (Exception e){
            logger.warn("每小时统计一次服务监控出错",e);
        }
    }


}
