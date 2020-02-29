package com.runcoding.monitor.sentinel;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/23 14:47
 * @describe: 通过系统的状态，例如 load1 等，来控制总的入口流量
 *这个 slot 会根据对于当前系统的整体情况，对入口的资源进行调配。其原理是让入口的 API 和当前系统的 API 达到一个动态平衡。
 *
 * 注意这个功能的两个限制:
 *
 * 只对入口流量起作用（调用类型为EntryType.IN），对出口流量无效。可通过SphU.entry()指定调用类型，如果不指定，默认是EntryType.OUT。
 *  Entry entry = SphU.entry("resourceName"，EntryType.IN);
 * 只在 Unix-like 的操作系统上生效
 **/
@RunWith(SpringRunner.class)
public class SentinelSystemTest {

    private static String sentinelResource = "order:add";

    private static AtomicInteger pass = new AtomicInteger();

    private static AtomicInteger block = new AtomicInteger();

    private static AtomicInteger total = new AtomicInteger();

    private  static  int latchCnt = 100;

    private CountDownLatch latch = new CountDownLatch(latchCnt);

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);


    private static void initSystemRule() {
        List<SystemRule> rules = new ArrayList<>();
        SystemRule rule = new SystemRule();
        // max load is 3
        rule.setHighestSystemLoad(3.0);
        // max avg rt of all request is 10 ms
        rule.setAvgRt(10);
        // max total qps is 20
        rule.setQps(20);
        // max parallel working thread is 10
        rule.setMaxThread(10);

        rules.add(rule);
        SystemRuleManager.loadRules(Collections.singletonList(rule));
    }
    @Test
    public void caseDegrade() throws Exception {
        initSystemRule();
        for (int i = 0; i < latchCnt; i++) {
            executorService.execute(()->{
                long start = System.currentTimeMillis();
                Entry entry = null;
                try {
                    entry = SphU.entry("methodA", EntryType.IN);
                    Node curNode = entry.getCurNode();
                    Node originNode = entry.getOriginNode();
                    System.out.println("curNode="+JSON.toJSONString(curNode));
                    System.out.println("originNode="+JSON.toJSONString(originNode));
                    // token acquired
                    int passIncr = pass.incrementAndGet();
                    System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,thread正常业务处理……pass="+passIncr);
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                    int blockIncr = block.incrementAndGet();
                    System.err.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,block!blockIncr="+blockIncr);
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        System.out.println(Thread.currentThread().getName()+",执行结束:"+(System.currentTimeMillis()-start)+"ms");
                        entry.exit();
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
    }





}
