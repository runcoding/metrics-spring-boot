package com.runcoding.monitor.web.dao;


import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2019/07/21 15:07
 * @description 启用系统监控
 * Copyright (C),
 **/
public interface MetricInfoMapper {

   List<MethodMetricInfo> findMetricInfo(@Param("orderType") Integer orderType);

   int insert(MethodMetricInfo methodMetricInfo);

   int delete(String delDate);

   int vacuum();

}
