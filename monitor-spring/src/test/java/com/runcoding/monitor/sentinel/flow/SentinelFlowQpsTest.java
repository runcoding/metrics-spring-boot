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
public class SentinelFlowQpsTest {

    private static String sentinelResource = "order:add";

    private  static  int latchCnt = 100;

    private CountDownLatch latch = new CountDownLatch(latchCnt);

    private static ExecutorService executorService = Executors.newFixedThreadPool(20);


    /**控制qps*/
    private void initFlowQpsRule() {
        FlowRule rule = new FlowRule();
        rule.setResource(sentinelResource);
        rule.setCount(5);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");

        // 匀速器模式下，设置了 QPS 为 5，则请求每 200 ms 允许通过 1 个 （发生拦截后是直接拒绝，还是排队等待，还是慢启动模式）
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        // 如果更多的请求到达，这些请求会被置于虚拟的等待队列中。等待队列有一个 max timeout，如果请求预计的等待时间超过这个时间会直接被 block
        // 在这里，timeout 为 1s
        rule.setMaxQueueingTimeMs(1 * 1000);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    @Test
    public void caseQps() throws InterruptedException {
        initFlowQpsRule();
        for (int i = 0; i < latchCnt; i++) {
             executorService.execute(()->{
                 // 资源名可使用任意有业务语义的字符串
                 if (SphO.entry(sentinelResource)) {
                     // 务必保证finally会被执行
                     try {
                         System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,正常业务处理……");
                     } finally {
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
