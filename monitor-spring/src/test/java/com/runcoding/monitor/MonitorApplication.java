
package com.runcoding.monitor;


import com.runcoding.monitor.web.model.MonitorConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient(autoRegister = false)
@EnableScheduling
/**
 * http://localhost:8080/swagger-ui.html
 */
@EnableSwagger2
public class MonitorApplication {


	public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MonitorApplication.class, args);
		System.out.println("http://localhost:"+ MonitorConstants.applicationPort+"/monitor/index.html?module=current/current#/");
    }
}
