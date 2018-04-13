package com.runcoding.monitor.config.sqlite;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;

/**
 * @module 主数据源配置
 * @author xukai
 * @date:  2018/04/06 21:07
 */
@Configuration
@MapperScan(basePackages = "com.runcoding.monitor.web.dao",
            sqlSessionFactoryRef = "sqliteMonitorSqlSessionFactory")
public class MonitorDataSourceConfiguration {

    private Logger  logger = LoggerFactory.getLogger(MonitorDataSourceConfiguration.class);

    /**
     * sqlite文件保持路径(默认当前用户home下)
     */
    @Value("${runcoding.monitor.sqlite.storePath:}")
    private String storePath;

    @Value("${spring.application.name:}")
    private   String appId;

    @Bean(name = "sqliteMonitorDataSource")
    public DataSource sqliteMonitorDataSource() {
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
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl("jdbc:sqlite:"+storeUrl);
        datasource.setDriverClassName("org.sqlite.JDBC");
        datasource.setValidationQuery("SELECT 1");
        return datasource;
    }

    @Bean(name = "sqliteMonitorTransactionManager")
    public DataSourceTransactionManager sqliteMonitorTransactionManager(@Qualifier("sqliteMonitorDataSource") DataSource mainDataSource) {
        return new DataSourceTransactionManager(mainDataSource);
    }

    @Bean(name = "sqliteMonitorSqlSessionFactory")
    @Primary
    public SqlSessionFactory sqliteMonitorSqlSessionFactory(@Qualifier("sqliteMonitorDataSource") DataSource mainDataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(mainDataSource);
        return sessionFactory.getObject();
    }


}
