package com.runcoding.monitor.sentinel.flow;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/23 14:47
 * @describe: 阿里监控哨兵
 **/
@RunWith(SpringRunner.class)
public class SentinelFlowThreadTest {

    private static String sentinelResource = "order:add";

    private  static  int latchCnt = 100;

    private CountDownLatch latch = new CountDownLatch(latchCnt);

    private static ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * 每秒运行1个线程执行一次
     * */
    private void initFlowThreadRule() {
        FlowRule rule = new FlowRule();
        rule.setResource(sentinelResource);
        rule.setCount(1);
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }
    /**
     *
     *  服务处理http 请求4个，每秒处理1个
     */
    @Test
    public void caseThread() throws Exception {
        executorService = Executors.newFixedThreadPool(4);
        initFlowThreadRule();
        for (int i = 0; i < latchCnt; i++) {
            executorService.execute(()->{
                if (SphO.entry(sentinelResource)) {
                    try {
                        System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,thread正常业务处理……");
                    }  finally {
                        SphO.exit();
                    }
                } else {
                    System.err.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,block!");
                }
                latch.countDown();
            });
        }
        latch.await();
    }





}
