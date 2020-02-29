package com.runcoding.monitor.web.model.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by runcoding on 2017/7/27.
 * 当前服务线程信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerThreadInfo {

   /**仍活动的线程总数*/
   private long threadCount;

   /**峰值*/
   private long peakThreadCount;

   /**线程总数（被创建并执行过的线程总数）*/
   private long  totalStartedThreadCount;

   /**当初仍活动的守护线程（daemonThread）总数*/
   private long daemonThreadCount;

   /**当前容器，线程分组后的信息*/
   private Map<String,List<GroupThreadInfo>> threadGroupInfo;


}
