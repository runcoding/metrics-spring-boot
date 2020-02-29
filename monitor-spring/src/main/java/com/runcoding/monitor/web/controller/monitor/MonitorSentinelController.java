package com.runcoding.monitor.web.controller.monitor;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.support.monitor.sentinel.SentinelRuleProcessor;
import com.runcoding.monitor.support.server.DynamicServerProcessor;
import com.runcoding.monitor.support.server.MonitorSessionService;
import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.model.sentinel.SentinelRuleInfo;
import com.runcoding.monitor.web.utils.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by runcoding on 2019/07/16.
 * @author runcoding
 */
@Slf4j
@Controller
@RequestMapping("admin/monitor")
public class MonitorSentinelController {

    @Autowired
    private MonitorSessionService sessionService;

    @Autowired
    private DynamicServerProcessor dynamicServerProcessor;

    /**获取Sentinel配置的规则信息*/
    @GetMapping(value = "/sentinel/rules")
    @ResponseBody
    public Resp getSentinelRules() {
        sessionService.isLogin();
        SentinelRuleInfo ruleInfo = SentinelRuleInfo.builder()
                .flowRules(FlowRuleManager.getRules())
                .degradeRules(DegradeRuleManager.getRules())
                .systemRules(SystemRuleManager.getRules()).build();

        ruleInfo.setAutoRule(MonitorConstants.isAutoRule);
        ruleInfo.setAuthorityWhite(SentinelRuleProcessor.getAuthorityWhite());
        ruleInfo.setAuthorityBlock(SentinelRuleProcessor.getAuthorityBlock());
        return Resp.success(ruleInfo);
    }

    /**编辑Sentinel配置的规则信息*/
    @PutMapping(value = "/sentinel/rule")
    @ResponseBody
    public Resp editSentinelRule(@RequestParam(value = "ruleType",defaultValue = "")  String ruleType ,
                                        @RequestParam(value = "slave",defaultValue = "false")boolean slave ,
                                        @RequestBody SentinelRuleInfo  ruleInfo) {
        sessionService.isLogin();
        if(StringUtils.isEmpty(ruleType)){
            return  Resp.failure("ruleType 为null");
        }
        if(!slave){
            Set<String> serverHostPorts = dynamicServerProcessor.getServerHostPorts();
            if(!CollectionUtils.isEmpty(serverHostPorts)){
                String token = sessionService.getToken();
                Set<String>  failHostPorts = new HashSet<>();
                serverHostPorts.forEach(hostPort->{
                    try {
                        String url = "http://"+hostPort+"/admin/monitor/sentinel/rule?ruleType="+ruleType+"&slave=true";
                        Resp result = RestTemplateUtils.putForEntity(url,token, JSON.toJSONString(ruleInfo));
                        if(result.isFailure()){
                            failHostPorts.add(hostPort);
                            log.error("更新Sentinel配置的规则信息失败,可能{}服务已经下线。{}",hostPort,JSON.toJSONString(result));
                        }
                    }catch (Exception e){
                        failHostPorts.add(hostPort);
                        log.error("更新Sentinel配置的规则信息失败,可能{}服务已经下线",hostPort,e);
                    }
                });
                serverHostPorts.removeAll(failHostPorts);
                return Resp.success(Lists.newArrayList(serverHostPorts,failHostPorts));
            }
        }
        boolean isOpsAll = StringUtils.endsWithIgnoreCase(ruleType,"all");
        /**修改系统规则*/
        if(StringUtils.endsWithIgnoreCase(ruleType,"systemRules") || isOpsAll ){
            SystemRuleManager.loadRules(ruleInfo.getSystemRules());
        }
        /**修改流控规则*/
        if(StringUtils.endsWithIgnoreCase(ruleType,"flowRules") || isOpsAll){
            SentinelRuleProcessor.loadFlowRules(ruleInfo.getFlowRules());
        }
        /**修改流控规则*/
        if(StringUtils.endsWithIgnoreCase(ruleType,"degradeRules") || isOpsAll){
            SentinelRuleProcessor.loadDegradeRules(ruleInfo.getDegradeRules());
        }
        /**是否自动开启自动限流*/
        if(StringUtils.endsWithIgnoreCase(ruleType,"timeOutSentinelAutoRule")){
            MonitorConstants.isAutoRule = ruleInfo.isAutoRule();
        }
        /**修改方法白名单*/
        if(StringUtils.endsWithIgnoreCase(ruleType,"authorityWhite")){
            SentinelRuleProcessor.setAuthorityWhite(ruleInfo.getAuthorityWhite());
        }
        /**修改方法白名单*/
        if(StringUtils.endsWithIgnoreCase(ruleType,"authorityBlock")){
            SentinelRuleProcessor.setAuthorityBlock(ruleInfo.getAuthorityBlock());
        }
        return Resp.success();
    }





}
