package com.runcoding.monitor.e2etest;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/30 17:17
 * @describe: 系统负载测试
 **/
public class SysLoadTest {

    /**模拟请求次数*/
    private  static  int latchCnt = 1;

    private CountDownLatch latch = new CountDownLatch(latchCnt);

    private AtomicLong   errCnt = new AtomicLong(0);

    /**模拟人数*/
    private static ExecutorService executorService = Executors.newFixedThreadPool(15);


    @Test
    public void loadAverage() {
        for (int i = 0; i <= latchCnt; i++) {
            executorService.execute(()->{
                try{
                    String res = MockHttpClient.getForEntity("http://localhost:8080/test/degrade", "", String.class);
                    System.out.println(String.format("threadId=%s,res=%s",Thread.currentThread().getId(),res));
                }catch (Exception e){
                    errCnt.getAndIncrement();
                    e.printStackTrace();
                    System.out.println(String.format("threadId=%s,res=%s",Thread.currentThread().getId(),"请求错误"));
                }finally {
                  latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("系统负载测试结束：latchCnt="+latchCnt+"，errCnt="+errCnt);
    }

}
