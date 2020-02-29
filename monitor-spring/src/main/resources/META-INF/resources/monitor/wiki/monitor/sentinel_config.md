## 动态规则编辑
[动态规则](https://github.com/alibaba/Sentinel/wiki/%E5%8A%A8%E6%80%81%E8%A7%84%E5%88%99%E6%89%A9%E5%B1%95)

## Sentinel框架限量指标从哪里来？

  - 系统负载值: ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()
     系统加载平均值是排队到可用处理器的可运行实体数目与可用处理器上可运行实体数目的总和在某一段时间进行平均的结果
  - qps: 每秒处理请求数(个)
  - 线程: 一个统计窗口期内，通过执行的线程数(个)
  - avgRt: 平均响应时间(ms)
  - resource: 匹配规则key(规则的资源描述)
     MonitorController.editSentinelRule(2), 为MonitorController类中，有两个参数的editSentinelRule方法
  - limitApp： 被限制的应用,授权时候为逗号分隔的应用集合，限流时为单个应用

> 备注：
  什么是在统计的窗口期？
  为框架处理限制规则提供衡量度量单位。如：统计连续6个请求的结果;

## 系统规则说明
```json
[
   {
     "resource": null,
     "limitApp": null,
     "highestSystemLoad": 10,
     "qps": -1,
     "avgRt": -1,
     "maxThread": -1
    },
    {
        "resource": null,
        "limitApp": null,
        "highestSystemLoad": -1,
        "qps": -1,
        "avgRt": -1,
        "maxThread": 100
    }
]
其中：highestSystemLoad=10, 为系统近一分钟的负载
     maxThread=100,在统计的窗口期
```

## 流控规则说明

```json
[
     {
        "resource": "MonitorController.containerMetrics(2)",
        "limitApp": "default",
        "grade": 0,
        "count": 200,
        "strategy": 0,
        "refResource": null,
        "controlBehavior": 0,
        "warmUpPeriodSec": 10,
        "maxQueueingTimeMs": 500
     },
     {
        "resource": "MonitorController.editSentinelRule(2)",
        "limitApp": "default",
        "grade": 1,
        "count": 100,
        "strategy": 0,
        "refResource": null,
        "controlBehavior": 0,
        "warmUpPeriodSec": 10,
        "maxQueueingTimeMs": 500
    }

]
 其中：
   grade=0时，通过线程数，count= 200 个(在统计窗口期内，通过200个线程)，
   grade=1时，通过QPS，count= 100 个(在统计窗口期内，通过100个请求)，
   maxQueueingTimeMs: 速率限制器行为中的最大排队时间(默认500ms)
```

## 降级规则说明

```json
[
  {
      "resource": "MonitorController.containerMetrics(2)",
      "limitApp": "default",
      "count": 3000,
      "timeWindow": 1000,
      "grade": 0,
      "cut": false,
      "passCount": 0
  },
  {
      "resource": "MonitorController.getSentinelRules(0)",
      "limitApp": "default",
      "count": 0.1,
      "timeWindow": 2000,
      "grade": 1,
      "cut": false,
      "passCount": 0
  },
]
 其中：
   grade=0时，RT降级。  count =3000 ms(响应超过3s该接口开始降级)，timeWindow=1000ms(在降级后1s后恢复)
   grade=1时，异常降级。 count =0.1 (异常比例,取值范围（0.0~1.0）)，timeWindow=2000ms(在降级后2s后恢复)
```

## 源码分析


### SystemRuleManager.java
```java

/**
     * Apply {@link SystemRule} to the resource. Only inbound traffic will be checked.
     *
     * @param resourceWrapper the resource.
     * @throws BlockException when any system rule's threshold is exceeded.
     */
    public static void checkSystem(ResourceWrapper resourceWrapper) throws BlockException {

        // 确定开关开了
        if (checkSystemStatus.get() == false) {
            return;
        }

        // for inbound traffic only
        if (resourceWrapper.getType() != EntryType.IN) {
            return;
        }

        // total qps
        double currentQps = Constants.ENTRY_NODE == null ? 0.0 : Constants.ENTRY_NODE.successQps();
        if (currentQps > qps) {
            throw new SystemBlockException(resourceWrapper.getName(), "qps");
        }

        // total thread
        int currentThread = Constants.ENTRY_NODE == null ? 0 : Constants.ENTRY_NODE.curThreadNum();
        if (currentThread > maxThread) {
            throw new SystemBlockException(resourceWrapper.getName(), "thread");
        }

        double rt = Constants.ENTRY_NODE == null ? 0 : Constants.ENTRY_NODE.avgRt();
        if (rt > maxRt) {
            throw new SystemBlockException(resourceWrapper.getName(), "rt");
        }

        // 完全按照RT,BBR算法来
        if (highestSystemLoadIsSet && getCurrentSystemAvgLoad() > highestSystemLoad) {
            if (currentThread > 1 &&
                currentThread > Constants.ENTRY_NODE.maxSuccessQps() * Constants.ENTRY_NODE.minRt() / 1000) {
                throw new SystemBlockException(resourceWrapper.getName(), "load");
            }
        }

    }

```

###  DegradeRule.java
```java
public boolean passCheck(Context context, DefaultNode node, int acquireCount, Object... args) {

        if (cut) {
            return false;
        }

        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(this.getResource());
        if (clusterNode == null) {
            return true;
        }

        if (grade == RuleConstant.DEGRADE_GRADE_RT) {
            double rt = clusterNode.avgRt();
            if (rt < this.count) {
                passCount.set(0);
                return true;
            }

            // Sentinel will degrade the service only if count exceeds.
            if (passCount.incrementAndGet() < RT_MAX_EXCEED_N) {
                return true;
            }
        } else {
            double exception = clusterNode.exceptionQps();
            double success = clusterNode.successQps();
            long total = clusterNode.totalQps();
            // if total qps less than RT_MAX_EXCEED_N, pass.
            if (total < RT_MAX_EXCEED_N) {
                return true;
            }

            if (success == 0) {
                return exception < RT_MAX_EXCEED_N;
            }

            if (exception / (success + exception) < count) {
                return true;
            }
        }

        synchronized (lock) {
            if (!cut) {
                // Automatically degrade.
                cut = true;
                ResetTask resetTask = new ResetTask(this);
                pool.schedule(resetTask, timeWindow, TimeUnit.SECONDS);
            }

            return false;
        }
    }
```

###  flow PaceController.java
```java
 @Override
    public boolean canPass(Node node, int acquireCount) {

        // 按照斜率来计算计划中应该什么时候通过
        long currentTime = TimeUtil.currentTimeMillis();

        long costTime = Math.round(1.0 * (acquireCount) / count * 1000);

        //期待时间
        long expectedTime = costTime + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            //这里会有冲突,然而冲突就冲突吧.
            latestPassedTime.set(currentTime);
            return true;
        } else {
            // 计算自己需要的等待时间
            long waitTime = costTime + latestPassedTime.get() - TimeUtil.currentTimeMillis();
            if (waitTime >= maxQueueingTimeMs) {
                return false;
            } else {
                long oldTime = latestPassedTime.addAndGet(costTime);
                try {
                    waitTime = oldTime - TimeUtil.currentTimeMillis();
                    if (waitTime >= maxQueueingTimeMs) {
                        latestPassedTime.addAndGet(-costTime);
                        return false;
                    }
                    Thread.sleep(waitTime);
                    return true;
                } catch (InterruptedException e) {
                }
            }
        }

        return false;
    }
```

### flow WarmUpController.java
```java
 @Override
    public boolean canPass(Node node, int acquireCount) {
        long passQps = node.passQps();

        long previousQps = node.previousPassQps();
        syncToken(previousQps);

        // 开始计算它的斜率
        // 如果进入了警戒线，开始调整他的qps
        long restToken = storedTokens.get();
        if (restToken >= warningToken) {
            long aboveToken = restToken - warningToken;
            // 消耗的速度要比warning快，但是要比慢
            // current interval = restToken*slope+1/count
            double warningQps = Math.nextUp(1.0 / (aboveToken * slope + 1.0 / count));
            if (passQps + acquireCount <= warningQps) {
                return true;
            }
        } else {
            if (passQps + acquireCount <= count) {
                return true;
            }
        }

        return false;
    }
```

