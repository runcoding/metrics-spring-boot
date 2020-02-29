package com.runcoding.monitor.web.controller.monitor;

import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.support.server.MonitorSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.web.bind.annotation.*;

/**
 * Created by runcoding on 2019/07/16.
 * @author runcoding
 * @desc 服务注册中心管理
 */
@Slf4j
@RestController
@RequestMapping("admin/monitor")
public class MonitorRegistrationController {

    @Autowired
    private MonitorSessionService sessionService;

    @Autowired(required = false)
    private ServiceRegistry serviceRegistry;

    @Autowired(required = false)
    private Registration registration;


    /**获取服务(Eureka)当前状态*/
    @GetMapping(value = "/register")
    public Resp getStatus(){
        sessionService.isLogin();
        Object status = serviceRegistry.getStatus(registration);
        return Resp.success(status);
    }

    /**变更服务(Eureka)当前状态
     * enum InstanceStatus {
     *         CANCEL_OVERRIDE,// Eureka 特有状态
     *         UP, // Ready to receive traffic(上线)
     *         DOWN, // Do not send traffic- healthcheck callback failed (下线)
     *         STARTING, // Just about starting- initializations to be done - do not send traffic
     *         OUT_OF_SERVICE, // Intentionally shutdown for traffic(一般会在发版部署时使用，让服务下线关闭流量)
     *         UNKNOWN;
     * */
    @PutMapping(value = "/register")
    public Resp setStatus(String status){
        sessionService.isLogin();
        serviceRegistry.setStatus(registration,status);
        return Resp.success("变更Eureka服务status="+status);
    }

    /**服务(Eureka)注册上线*/
    @PostMapping("/register")
    public Resp register(){
        sessionService.isLogin();
        serviceRegistry.register(registration);
        return Resp.success("服务成功注册上线");
    }

    /**
     * 服务(Eureka)注销下线
     * https://www.cnblogs.com/trust-freedom/p/10744683.html
     * 此处可能有坑，调用注销下线，可能会导致再次调用上线无效(无法成功注册到Eureka Server)
     * */
    @PostMapping(value = "/deregister")
    public Resp deregister(){
        sessionService.isLogin();
        serviceRegistry.deregister(registration);
        return Resp.success("服务成功注销下线(无法重新恢复注册)");
    }



}
