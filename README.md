# metrics-spring-boot
Metrics Spring-boot Sqlite

## 服务接口监控
 - 查看当前服务内存、线程运行情况。
 - 每小时定时统计接口调用情况。
 
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

```html
- http://localhost:8080/monitor/index.html
<img src='https://github.com/runcoding/metrics-spring-boot/raw/master/snapshot/monitor_index.png'>
- http://localhost:8080/monitor/metric.html
<img src='https://github.com/runcoding/metrics-spring-boot/raw/master/snapshot/monitor_metric.png'>
```

