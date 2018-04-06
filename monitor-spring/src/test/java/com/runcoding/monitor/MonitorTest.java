package com.runcoding.monitor;

import com.alibaba.fastjson.JSON;
import com.runcoding.monitor.web.dao.MetricInfoMapper;
import com.runcoding.monitor.web.model.metrics.MetricInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.ref.SoftReference;
import java.util.List;

/**
 * @desc 服务
 * @author xukai
 * @date: 2018年01月31日
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MonitorApplication.class)
public class MonitorTest {

    private final Logger logger = LoggerFactory.getLogger(MonitorTest.class);

    @Autowired
    private MetricInfoMapper serviceAnalysisMapper;

    @Test
    public void findMetricInfo() {
      List<MetricInfo> list = serviceAnalysisMapper.findMetricInfo();
        logger.info(JSON.toJSONString(list));
    }

    @Test
    public void  insert(){
        MetricInfo serviceAnalysis = new MetricInfo();
        serviceAnalysis.setName("insert");
        serviceAnalysis.setMinuteRate(1.2);
        serviceAnalysis.setRefDate("2018-03-29 00:00:00");
        serviceAnalysis.setCount(1L);
        serviceAnalysisMapper.insert(serviceAnalysis);
    }

    @Test
    public void delete(){
        serviceAnalysisMapper.delete("2018-03-30 00:00:00");
    }


}
