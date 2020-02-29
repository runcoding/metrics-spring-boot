package com.runcoding.monitor.config.sqlite;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * @module 主数据源配置
 * @author runcoding
 * @date: 2017年10月27日
 */
@Configuration
@MapperScan(basePackages = "com.runcoding.monitor.web.dao",
            sqlSessionFactoryRef = "sqliteMonitorSqlSessionFactory")
public class MonitorDataSourceConfiguration {

    private Logger  logger = LoggerFactory.getLogger(MonitorDataSourceConfiguration.class);

    /** sqlite文件保持路径(默认当前用户home下) */
    @Value("${runcoding.monitor.sqlite.storePath:}")
    private String storePath;

    @Value("${spring.application.name:}")
    private   String appId;

    private DruidDataSource dataSource;


    @Bean(name = "sqliteMonitorDataSource")
    public  DruidDataSource  sqliteMonitorDataSource() {
        appId = StringUtils.isEmpty(appId) ? "monitor.db": appId+".db";
        if(StringUtils.isEmpty(storePath)){
            String userHome = System.getenv("HOME");
            storePath = userHome + "/data/sqlite/monitor";
        }
        File file = new File(storePath);
        if(!file.exists()){
           file.mkdirs();
        }
        String storeUrl = storePath + "/" + appId ;
        logger.info("monitor统计数据存储路径:"+storeUrl);
        dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:sqlite:"+storeUrl);
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    @Bean(name = "sqliteMonitorTransactionManager")
    public DataSourceTransactionManager sqliteMonitorTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqliteMonitorSqlSessionFactory")
    public SqlSessionFactory sqliteMonitorSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        return sessionFactory.getObject();
    }


}
