package com.runcoding.web.controller;

import com.runcoding.web.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author: xukai
 * @email: runcoding@163.com
 * @created Time: 2018/4/13 21:28
 * @description run home
 **/
@Controller
public class RunController {

    private static int init  = 50;

    private static ExecutorService executorService = new ScheduledThreadPoolExecutor(init);

    @Autowired
    private RunService runService;

    @RequestMapping(value = "/run",method = RequestMethod.GET)
    @ResponseBody
    public boolean saveRun() {
        for (int i = 0; i < init * init ; i++) {
            executorService.execute(() -> {
                runService.saveRun();
            });
        }
        return true;
    }

    @RequestMapping("/")
    public String home(){
        return "redirect:/monitor/metric.html";
    }

}
