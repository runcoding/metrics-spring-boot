package com.runcoding.monitor.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/23 14:47
 * @describe:熔断降级
 * 主要针对资源的运行 RT 以及预设规则（平均 RT 模式或异常比率模式），来决定资源是否在接下来的时间被自动降级掉。
 **/
@RunWith(SpringRunner.class)
public class SentinelDegradeTest {

    private static String sentinelResource = "order:add";

    private static AtomicInteger pass = new AtomicInteger();

    private static AtomicInteger block = new AtomicInteger();

    private static AtomicInteger total = new AtomicInteger();

    private  static  int latchCnt = 100;

    private CountDownLatch latch = new CountDownLatch(latchCnt);

    private static ExecutorService executorService = Executors.newFixedThreadPool(20);



    /**
     * 熔断降级(根据平均响应时间 (DEGRADE_GRADE_RT))
     * https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7
     *    当资源的平均响应时间超过阈值（DegradeRule 中的 count，以 ms 为单位）之后，资源进入准降级状态。
     *    接下来如果持续进入 5 个请求，它们的 RT 都持续超过这个阈值，
     *    那么在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，
     *    对这个方法的调用都会自动地返回。
     *
     * */
    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(sentinelResource);
        rule.setCount(20); //当资源的平均响应时间超过阈值20ms
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setTimeWindow(2); //降级时间2s(2s时间都会进入阻塞)
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }

    @Test
    public void caseDegrade() throws Exception {
        executorService = Executors.newFixedThreadPool(2);
        initDegradeRule();
        for (int i = 0; i < latchCnt; i++) {
            executorService.execute(()->{
                long start = System.currentTimeMillis();
                Entry entry = null;
                try {
                   // TimeUnit.MILLISECONDS.sleep(5);
                    entry = SphU.entry(sentinelResource);
                    // token acquired
                    int passIncr = pass.incrementAndGet();
                    System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,thread正常业务处理……pass="+passIncr);
                    // sleep 15 ms, as rt
                   // TimeUnit.MILLISECONDS.sleep(600);
                } catch (Exception e) {
                    int blockIncr = block.incrementAndGet();
                    System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,block!blockIncr="+blockIncr);
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        System.err.println(Thread.currentThread().getName()+",执行结束:"+(System.currentTimeMillis()-start)+"ms");
                        entry.exit();

                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
    }


    /**
     * 熔断降级{异常比例 (DEGRADE_GRADE_EXCEPTION)}
     * https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7
     *   当资源的每秒异常总数占通过总数的比值超过阈值（DegradeRule 中的 count）之后，(第一次统计5次)
     *   资源进入降级状态，
     *   即在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，
     *   对这个方法的调用都会自动地返回。
     *
     * */
    private static void initDegradeExceptionRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(sentinelResource);
        rule.setCount(0.3); //每秒异常总数占通过总数的比值超过阈值
        //rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION);
        rule.setTimeWindow(2); //降级时间2s(2s时间都会进入阻塞)
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }

    @Test
    public void caseDegradeException() throws Exception {
        executorService = Executors.newFixedThreadPool(2);
        initDegradeExceptionRule();
        for (int i = 0; i < latchCnt; i++) {
            int finalI = i;
            executorService.execute(()->{
                Entry entry = null;
                try {
                    entry = SphU.entry(sentinelResource, EntryType.IN);
                    System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,thread正常业务处理……pass");
                    if (finalI % 2 == 0) {
                        System.out.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,模拟异常业务");
                        // biz code raise an exception.
                        throw new RuntimeException("throw runtime ");
                    }
                } catch (Throwable t) {
                    if (!BlockException.isBlockException(t)){
                        Tracer.trace(t);
                    }else if(t instanceof  BlockException){
                        System.err.println(Thread.currentThread().getName()+",在"+LocalDateTime.now().toString()+"s,block!block");
                    }
                } finally {
                    if (entry != null) {
                        entry.exit();
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
    }






}
