package com.runcoding.monitor.web.controller.monitor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: xukai
 * @email: runcoding@163.com
 * @created Time: 2018/4/13 21:28
 * @description run home
 **/
@Controller
public class RunController {

    @RequestMapping("/")
    public String home(){
        return "redirect:/monitor/metric.html";
    }

}
