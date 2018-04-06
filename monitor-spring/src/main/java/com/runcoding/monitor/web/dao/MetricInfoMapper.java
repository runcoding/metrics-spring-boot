package com.runcoding.monitor.web.dao;


import com.runcoding.monitor.web.model.metrics.MetricInfo;

import java.util.List;

/**
 * @author: xukai
 * @email: runcoding@163.com
 * @created Time: 2018/03/21 15:07
 * @description 启用系统监控
 *
 **/
public interface MetricInfoMapper {

   List<MetricInfo> findMetricInfo( );

   int insert(MetricInfo  metricInfo);

   int delete(String delDate);

}
