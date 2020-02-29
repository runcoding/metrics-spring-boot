package com.runcoding.monitor.web.controller.monitor;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.support.server.DynamicServerProcessor;
import com.runcoding.monitor.support.server.MonitorSessionService;
import com.runcoding.monitor.web.dao.MetricInfoMapper;
import com.runcoding.monitor.web.job.MonitorJob;
import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.model.container.ContainerRunningInfo;
import com.runcoding.monitor.web.model.container.GarbageCollectorInfo;
import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import com.runcoding.monitor.web.utils.RestTemplateUtils;
import com.runcoding.monitor.web.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by runcoding on 2017/10/26.
 * @author runcoding
 */
@Slf4j
@Controller
@RequestMapping("admin/monitor")
public class MonitorController {

    @Autowired
    private MonitorSessionService sessionService;

    @Autowired
    private ContextRefresher contextRefresher;

    @Autowired
    private MetricInfoMapper metricInfoMapper;

    @Autowired
    private MonitorJob monitorJob;

    @Autowired
    private DynamicServerProcessor dynamicServerProcessor;

    /**refresh lock*/
    private static ReentrantLock refreshLock   = new ReentrantLock();

    /**当前服务运行指标*/
    @GetMapping(value = "/job" )
    @ResponseBody
    public Resp job(){
        sessionService.isLogin();
        monitorJob.monitorHour();
        return Resp.success();
    }

    @PutMapping(value = "/refresh")
    @ResponseBody
    public Resp refresh(){
        sessionService.isLogin();
        /**触发刷新*/
        String token = sessionService.getToken();
        Map<String,Object> successHostPorts = Maps.newHashMap();
        Map<String,Object> failHostPorts = Maps.newHashMap();
        dynamicServerProcessor.getServerHostPorts().forEach(hostPort->{
            try {
                String url = "http://"+hostPort+"/admin/monitor/refresh/slave";
                Resp result = RestTemplateUtils.putForEntity(url,token,null);
                if(result.isFailure()){
                    failHostPorts.put(hostPort,result.getMessage());
                    log.error("refresh配置的规则信息失败,可能{}服务已经下线。{}",hostPort,JSON.toJSONString(result));
                }
                successHostPorts.put(hostPort, result.getData());
            }catch (Exception e){
                failHostPorts.put(hostPort,e.getMessage());
                log.error("refresh配置的规则信息失败,可能{}服务已经下线",hostPort,e);
            }
        });
        return Resp.success(Lists.newArrayList(successHostPorts,failHostPorts));

    }


    @PutMapping(value = "/refresh/slave")
    @ResponseBody
    public Resp refreshSlave(){
        sessionService.isLogin();
        if(!refreshLock.tryLock()){
           return Resp.failure("节点正在刷新中……");
        }
        try{
            MonitorConstants.isRefresh = true;
            Set<String> keys = contextRefresher.refresh();
            return Resp.success(keys);
        }catch (Exception e){
            return Resp.failure("节点更新失败。"+e.getMessage());
        }finally {
            MonitorConstants.isRefresh = false;
            refreshLock.unlock();
        }
    }

    /**当前服务运行指标*/
    @GetMapping(value = "/current_metrics" )
    @ResponseBody
    public Resp<ContainerRunningInfo> containerMetrics(
            @RequestParam(value = "isDumpAllThread",defaultValue = "false") boolean isDumpAllThread,
            @RequestParam(value = "proposalMethodName",defaultValue = "") String proposalMethodName) {
        sessionService.isLogin();
        ContainerRunningInfo runningInfo = ContainerRunningInfo.buildContainerRunningInfo(isDumpAllThread, proposalMethodName);
        return  Resp.success(runningInfo);
    }


    /**历史接口统计信息*/
    @GetMapping(value = "/method-metric")
    @ResponseBody
    public Resp queryApiAnalysisList(int orderType) {
        sessionService.isLogin();
        List<MethodMetricInfo> list = metricInfoMapper.findMetricInfo(orderType);
        return Resp.success(list);
    }

    /**获取服务注册负载*/
    @GetMapping(value = "/server-host-port")
    @ResponseBody
    public Resp<Set<String>>  getServerHostPorts(){
        Set<String> serverHostPorts = dynamicServerProcessor.getServerHostPorts();
        return Resp.success(serverHostPorts);
    }

    /**
     * 获取top 10的线程
     * @param interval  间隔周期毫秒
     * @param top 获取Cup 占用耗时最长的多少个
     * */
    @GetMapping("/thread/cpu/top")
    @ResponseBody
    public Resp getCupTopThreads(@RequestParam(name = "interval",defaultValue = "1000") int interval,
                                         @RequestParam(name = "top",defaultValue = "10") int top) {
        sessionService.isLogin();
        Map<String, String> topNThreads = ThreadUtil.getTopNThreads(interval, top);
        return Resp.success(topNThreads);
    }

    @GetMapping("/gc")
    @ResponseBody
    public Resp getGarbageCollector(){
        sessionService.isLogin();
        List<GarbageCollectorInfo> gcList = ManagementFactory.getGarbageCollectorMXBeans().stream().map(gc ->
                        GarbageCollectorInfo.builder().gcName(gc.getName()).gcCount(gc.getCollectionCount()).gcTime(gc.getCollectionTime()).build()
                ).collect(Collectors.toList());
        return Resp.success(gcList);
    }

    /**是否已经登陆*/
    @GetMapping(value = "/login" )
    @ResponseBody
    public Resp isLogin(){
        sessionService.isLogin();
        return Resp.success();
    }

}
