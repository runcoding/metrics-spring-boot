package com.runcoding.monitor.test;

import org.springframework.stereotype.Service;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/11 17:08
 * @describe: 1
 **/
@Service
public class MonitorTestService {


    public void degrade(Long degrade) throws InterruptedException {
        Thread.sleep(degrade);
    }

}
