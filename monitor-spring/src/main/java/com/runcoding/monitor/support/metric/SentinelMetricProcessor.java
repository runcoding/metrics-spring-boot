package com.runcoding.monitor.support.metric;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter;
import com.alibaba.csp.sentinel.transport.util.HttpCommandUtils;
import com.google.common.collect.Lists;
import com.runcoding.monitor.web.utils.date.DatePattern;
import com.runcoding.monitor.web.utils.date.LocalDateUtil;
import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import com.runcoding.monitor.web.model.MonitorConstants;

import java.util.List;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/29 11:51
 * @describe: 监控哨兵统计信息
 **/
public class SentinelMetricProcessor {


    /**
     * 指定的开始时间(startTime)，
     * 结束时间(endTime) {endTime ==null时，可以设置maxLines最大行数为}
     * 指定资源(identity)的值
     * @param request
     * @return
     */
    public static List<MethodMetricInfo> getMethodMetricInfo(CommandRequest request)  {
        List<MethodMetricInfo> methodApis = Lists.newArrayList();
        request.addMetadata(HttpCommandUtils.REQUEST_TARGET,"metric");
        CommandHandler<?> commandHandler = SimpleHttpCommandCenter.getHandler("metric");
        if (commandHandler == null) {
           return methodApis;
        }

        CommandResponse<String> response = (CommandResponse<String>) commandHandler.handle(request);
        if(!response.isSuccess()){
            return methodApis;
        }
        String result = response.getResult();
        String[] metricArr = result.split("\n");
        if(metricArr == null ){
            return methodApis;
        }
        for (int i = 0; i < metricArr.length ; i++) {
            String metricLine = metricArr[i];
            String[] metric = metricLine.split("\\|");
            if(metric == null || metric.length < 7){
                continue;
            }
            /**1535513607000|MonitorController.containerMetrics(1)|63|0|64|0|4512*/
            MethodMetricInfo methodMetricInfo = new MethodMetricInfo();
            String refDate = LocalDateUtil.secondToStr(Long.parseLong(metric[0]) / MonitorConstants.millisecond, DatePattern.LONG);
            methodMetricInfo.setRefDate(refDate);
            methodMetricInfo.setName(metric[1]);
            methodMetricInfo.setCntPassRequest(Long.parseLong(metric[2]));
            methodMetricInfo.setCntBlockRequest(Long.parseLong(metric[3]));
            methodMetricInfo.setCntRequest(Long.parseLong(metric[2])+Long.parseLong(metric[3]));
            methodMetricInfo.setCntSuccessRequest(Long.parseLong(metric[4]));
            methodMetricInfo.setCntExceptionRequest(Long.parseLong(metric[5]));
            methodMetricInfo.setAvgRt(Long.parseLong(metric[6]));
            methodApis.add(methodMetricInfo);
        }
        return methodApis;
    }

}
