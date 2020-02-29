package com.runcoding.monitor.web.model.sentinel;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @user: runcoding
 * @author: runcoding@163.com
 * @date: 2018/08/24 17:46
 * @describe: Sentinel规则配置
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentinelRuleInfo {


    /**是否自动开启限流*/
    private  boolean autoRule ;

    /**执行方法白名单{key:执行方法,value:规则截止时间，-1不限制(单位ms)}*/
    private  Map<String,Long> authorityWhite;

    /**执行方法黑名单{key:执行方法,value:规则截止时间，-1不限制(单位ms)}*/
    private  Map<String,Long> authorityBlock;

    /**修改流控规则*/
    private List<FlowRule> flowRules;

    /**修改降级规则*/
    private List<DegradeRule> degradeRules;

    /**修改系统规则*/
    private List<SystemRule> systemRules;


}
