package com.runcoding.monitor.config.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2019/07/25 21:07
 * @description sqlite初始化
 * Copyright (C),
 **/
@Configuration
public class DBInitializeConfig {

	private Logger logger = LoggerFactory.getLogger(DBInitializeConfig.class);

	/**初始化脚步存放路径*/
	@Value("${runcoding.monitor.sqlite.store_schema_path:/store/schema.sql}")
	private  String store_schema_path ;

	@Autowired
	@Qualifier("sqliteMonitorDataSource")
	private DataSource dataSource;
	
	@PostConstruct
	public void initialize(){
        Connection connection = null;
        Statement  statement  = null;
		try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
			statement.executeUpdate("BEGIN IMMEDIATE;");
			initSqliteSchema(statement,store_schema_path);
			statement.executeUpdate("COMMIT;");
		} catch (SQLException e) {
			logger.error("初始化统计数据异常:",e);
		} finally {
            try {
                if(connection != null && !connection.isClosed()){
                   connection.close();
                }
                if (statement != null && !statement.isClosed()){
                   statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}

	public static void  initSqliteSchema(Statement statement ,String localSqlitePath) throws SQLException {
		String sql = null;
		try {
			sql = readResourceFileContent(localSqlitePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] sqlArr = sql.split(";");
		for (String sqlStr : sqlArr) {
			if(!StringUtils.isEmpty(sqlStr.trim())){
				statement.execute(sqlStr.trim());
			}
		}
	}

	public static String readResourceFileContent(String resourceFilePath) throws IOException {
		StringBuilder sb = new StringBuilder(8192);

		try (InputStream inputStream = DBInitializeConfig.class.getResourceAsStream(resourceFilePath)) {
			try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
				BufferedReader r = new BufferedReader(reader);
				String str = null;

				while ((str = r.readLine()) != null) {
					sb.append(str);
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}
}
