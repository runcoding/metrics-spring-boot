# metrics-spring-boot
Metrics Spring-boot Sqlite Aop method tps

## 服务接口监控
 - 查看当前服务内存、线程运行情况。
 - 每小时定时统计接口调用(count、tps)情况。
 
注意： 查看统计运行结果时，开启ManagementFactory.getThreadMXBean().dumpAllThreads(false, false);会降低服务性能。
 
## 使用
 ```xml
  <dependency>
        <groupId>com.runcoding</groupId>
        <artifactId>monitor-spring</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
```
## 动态配置参数
```text
runcoding.monitor.sqlite.storePath = ## 默认  当前用户目录 + /data/sqlite/monitor
spring.application.name = ## 数据文件名称，默认 monitor.db
```
## 查看统计
run monitor-spring-test
### 时时查看JVM内存、线程、接口或服务调用次数 
- 地址： http://localhost:8080/monitor/index.html
![](snapshot/monitor_index.png) 
### 查看每分钟接口调用速率均值(每小时统计一次)   
- 地址： http://localhost:8080/monitor/metric.html
![](snapshot/monitor_metric.png)
 

 