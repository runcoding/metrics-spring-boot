package com.runcoding.monitor.support.server;

import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.utils.IpUtils;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/04 10:33
 * @describe: 服务信息
 **/
@Service
public class DynamicServerProcessor   implements ApplicationContextAware {

    private Logger  logger  = LoggerFactory.getLogger(DynamicServerProcessor.class);

    /**上下文对象实例 */
    private ApplicationContext applicationContext;

    /** 获取服务注册在eureka */
    public Set<String>  getServerHostPorts(){
         SpringClientFactory springClientFactory = applicationContext.getBean(SpringClientFactory.class);
         Set<String>      hostPorts = new HashSet<>();
         ILoadBalancer loadBalancer = springClientFactory.getLoadBalancer(MonitorConstants.applicationName);
         if(loadBalancer != null){
             List<Server> reachableServers = loadBalancer.getReachableServers();
             reachableServers.forEach(e->{
                 /**如果服务有设置zone，此处获取的可能并不是所有的实例
                  * http://www.cnblogs.com/flying607/p/9560159.html
                  * @EnableDiscoveryClient 开启(EurekaRibbonClientConfiguration)或关闭
                  * */
                 hostPorts.add(e.getHostPort());
             });
         }
         if(CollectionUtils.isEmpty(hostPorts)){
            hostPorts.add(IpUtils.getIp()+":"+MonitorConstants.applicationPort);
         }
         return hostPorts;
    }

    /**获取bean*/
    public <T> T getBean(Class<T> requiredType) throws BeansException{
        return applicationContext.getBean(requiredType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
