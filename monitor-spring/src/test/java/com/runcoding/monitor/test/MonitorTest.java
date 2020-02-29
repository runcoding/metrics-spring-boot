package com.runcoding.monitor.test;

import com.alibaba.fastjson.JSON;
import com.runcoding.monitor.MonitorApplication;
import com.runcoding.monitor.web.dao.MetricInfoMapper;
import com.runcoding.monitor.web.job.MonitorJob;
import com.runcoding.monitor.web.model.metrics.MethodMetricInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @desc 服务
 * @author runcoding
 * @date: 2018年01月31日
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MonitorApplication.class)
public class MonitorTest {

    private final Logger logger = LoggerFactory.getLogger(MonitorTest.class);

    @Autowired
    private MetricInfoMapper metricInfoMapper;

    @Autowired
    private MonitorJob monitorJob;

    @Test
    public void exec(){
        monitorJob.monitorHour();
    }

    @Test
    public void findMetricInfo() {
      List<MethodMetricInfo> list = metricInfoMapper.findMetricInfo(0);
        logger.info(JSON.toJSONString(list));

    }

    @Test
    public void  insert(){
        for (int i = 0; i < 1000 ; i++) {
            try{
                MethodMetricInfo serviceAnalysis = new MethodMetricInfo();
                serviceAnalysis.setName("insert");
                serviceAnalysis.setRefDate("2018-04-12 00:00:00"+i);
                serviceAnalysis.setCntRequest(1L);
                metricInfoMapper.insert(serviceAnalysis);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        /**清空7天之前的数据*/
        int deleteNum = metricInfoMapper.delete(LocalDateTime.now().plusDays(-7).
                format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if(deleteNum>0){
            /**释放磁盘空间*/
            int vacuum = metricInfoMapper.vacuum();
            System.out.println("vacuum="+vacuum);
        }
    }

    @Test
    public void delete(){
        metricInfoMapper.delete("2018-03-30 00:00:00");
    }
}
