package com.runcoding.monitor.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2019/07/21 15:07
 * @description 启用系统监控
 * Copyright (C),
 **/
@Configuration
@ComponentScan({"com.runcoding.monitor"})
@PropertySource("classpath:/monitor_config.properties")
public class MonitorConfiguration {


}
