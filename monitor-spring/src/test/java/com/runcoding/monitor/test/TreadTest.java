package com.runcoding.monitor.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/30 17:17
 * @describe: 系统负载测试
 **/
public class TreadTest {

    private  static  int latchCnt = 100;

    private CountDownLatch latch = new CountDownLatch(latchCnt);

    private Map<Long ,Thread> threadMap = new HashMap<>();

    private static AtomicLong processThreads   = new AtomicLong();

    private static ExecutorService executorService = new ThreadPoolExecutor(
            10,
            /**最大3个线程*/
            10,
            5000L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(16),
            /**拒绝策略使用当前运行线程执行，放弃异步执行使用同步*/
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Test
    public void loadAverage() throws InterruptedException {
        for (int i = 0; i <= latchCnt; i++) {
            executorService.execute(()->{
                   Thread thread = Thread.currentThread();
                try{
                    System.out.println(String.format(" exec threadId=%s",thread.getId()));
                    long andIncrement = processThreads.getAndIncrement();
                    if( andIncrement%2 == 1){
                        threadMap.put(thread.getId(),thread);
                        Thread.sleep(2000);
                    }
                    if( andIncrement%2 == 0){
                        if(!threadMap.values().isEmpty()){
                            Thread thread1 = threadMap.values().iterator().next();
                            System.out.println(String.format("interrupt andIncrement=%s threadId=%s",andIncrement,thread1.getId()));
                            threadMap.remove(thread1.getId());
                            thread1.interrupt();
                        }
                    }
                }catch (Exception e){
                    System.err.println(String.format("error threadId=%s ,e=%s",thread.getId(),e.getMessage()));
                } finally{
                   latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println("threadMap="+JSON.toJSONString(threadMap,SerializerFeature.DisableCircularReferenceDetect));
    }

}
