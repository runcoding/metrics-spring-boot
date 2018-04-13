package com.runcoding.monitor.support.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.runcoding.monitor.web.model.metrics.MetricInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author xukai
 * @desc 统计
 */
public class MetricProcessor {

    private static MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    public static MetricRegistry getMetricRegistry() {
        return METRIC_REGISTRY;
    }

    /**清理全部*/
    public static List<MetricInfo> clearAll() {
        SortedMap<String, Timer> timers = METRIC_REGISTRY.getTimers();
        List<MetricInfo> methodApis = new ArrayList<>(timers.size());
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (Map.Entry<String, Timer> entrys : timers.entrySet()) {
            Timer timer = entrys.getValue();
            double mean = timer.getSnapshot().getMean()/1000000000;
            methodApis.add(new MetricInfo(nowDate,entrys.getKey(), timer.getCount(), timer.getOneMinuteRate(),mean));
            METRIC_REGISTRY.remove(entrys.getKey());
        }
        return methodApis;
    }


}
