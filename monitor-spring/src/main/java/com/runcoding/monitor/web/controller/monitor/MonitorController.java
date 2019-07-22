package com.runcoding.monitor.web.controller.monitor;

import com.runcoding.monitor.support.jvm.JvmProcessor;
import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.web.dao.MetricInfoMapper;
import com.runcoding.monitor.web.model.endpoint.MonitorResp;
import com.runcoding.monitor.web.model.jvm.ContainerRunningInfo;
import com.runcoding.monitor.web.model.metrics.MetricInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by xukai on 2017/10/26.
 */
@Controller
@RequestMapping("admin/monitor")
public class MonitorController {

    @Autowired
    private MetricInfoMapper metricInfoMapper;

    @GetMapping(value = "/metrics" )
    public String metrics() {
        return  "monitor/index";
    }


    @GetMapping(value = "/metrics_jvm" )
    @ResponseBody
    public MonitorResp<ContainerRunningInfo> jvmInfo() {
        ContainerRunningInfo containerRunningInfo = ContainerRunningInfo.buildContainerRunningInfo();
        return  MonitorResp.success(containerRunningInfo);
    }

    @DeleteMapping(value = "/api_metrics_clear")
    @ResponseBody
    public  MonitorResp apiMetricsClear() {
        List<MetricInfo> metricInfos = MetricProcessor.clearAll();
        metricInfos.forEach(metricInfo -> {
            metricInfoMapper.insert(metricInfo);
        });
        return MonitorResp.success(JvmProcessor.getIp());
    }

    @GetMapping(value = "/api_analysis")
    @ResponseBody
    public MonitorResp queryApiAnalysisList(int orderType) {
        List<MetricInfo> list = metricInfoMapper.findMetricInfo(orderType);
        return MonitorResp.success(list);
    }


}
