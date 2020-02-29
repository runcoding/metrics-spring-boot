package com.runcoding.monitor.test;

import com.alibaba.csp.sentinel.util.HostNameUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/24 14:59
 * @describe:  系统负载测试
 **/
@RunWith(SpringRunner.class)
public class LoadAverage {

  private  static  int latchCnt = 2048;

  private CountDownLatch latch = new CountDownLatch(latchCnt);


  private static ExecutorService executorService = Executors.newFixedThreadPool(20);

  @Test
  public   void loadAverage() throws InterruptedException {
      OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

      double  cpu = os.getSystemLoadAverage()/ os.getAvailableProcessors();
      System.out.println("Load Average: " + os.getSystemLoadAverage()+",="+cpu);
      for (int i = 0; i <= latchCnt; i++) {
          executorService.execute(()->{
              ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean().dumpAllThreads(false, false);
              latch.countDown();
          });
      }
      latch.await();
      os = ManagementFactory.getOperatingSystemMXBean();
      cpu = os.getSystemLoadAverage()/ os.getAvailableProcessors();
      System.out.println("Load Average: " + os.getSystemLoadAverage()+",="+cpu+"="+HostNameUtil.getConfigString());


      //获取young GC 和full GC 次数
      List<GarbageCollectorMXBean> list1=ManagementFactory.getGarbageCollectorMXBeans();
      for(GarbageCollectorMXBean e:list1){
          System.out.println(String.format("name=%s,count=%s,time=%s",e.getName(),e.getCollectionCount(),e.getCollectionTime()));
      }
  }
}
 