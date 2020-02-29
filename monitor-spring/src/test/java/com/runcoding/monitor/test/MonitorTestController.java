package com.runcoding.monitor.test;

import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.web.utils.IPAddressAnalysor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/11 17:10
 * @describe:
 **/
@Controller
@RequestMapping("test")
@RefreshScope
public class MonitorTestController {

    private Logger  logger = LoggerFactory.getLogger(MonitorTestController.class);

    @Autowired
    private MonitorTestService monitorTestService;


    /**配置最高系统加载平均值，是排队到可用处理器的可运行实体数目与可用处理器上可运行实体数目的总和在某一段时间进行平均的结果*/
    @Value("${csp.sentinel.rules.system.systemLoad:-1}")
    private double highestSystemLoad ;

    /**
     * 测试降级方法
     * siege -c 100 -r 10000 "http://localhost:8080/test/degrade?degrade=10"
     * */
    @GetMapping(value = "/degrade" )
    @ResponseBody
    public Resp degrade(boolean isNull, @RequestParam(value = "degrade",defaultValue = "200")Long degrade,
                        HttpServletRequest request) throws InterruptedException {
        if(isNull){
            throw new NullPointerException("aa");
        }
        String ipAddress = IPAddressAnalysor.getIPAddress(request);
        logger.info("ip={}",ipAddress);
        monitorTestService.degrade(degrade);
        return Resp.success();
    }




}
