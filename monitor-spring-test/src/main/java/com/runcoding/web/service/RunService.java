package com.runcoding.web.service;

import com.alibaba.druid.sql.visitor.functions.Char;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: xukai
 * @email: runcoding@163.com
 * @created Time: 2018/4/13 21:30
 * @description run 测试
 **/
@Service
public class RunService {

    private Logger  logger = LoggerFactory.getLogger(RunService.class);

    private AtomicInteger  num  = new AtomicInteger(0);

    /**模拟保存*/
    public boolean saveRun(){
        logger.info("模拟保存{}次。",num.incrementAndGet());
        try {

            Thread.sleep(new Random().nextInt(1000));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

}
