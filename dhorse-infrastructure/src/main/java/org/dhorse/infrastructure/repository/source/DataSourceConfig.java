package org.dhorse.infrastructure.repository.source;

import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.component.MysqlConfig;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.DateClass;
import org.sqlite.SQLiteDataSource;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;


@Configuration
@EnableTransactionManagement
@MapperScan("org.dhorse.infrastructure.repository.mapper")
public class DataSourceConfig{
	
	private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

	private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
	
	private static final String TEST_QUERY = "SELECT 1";
	
	private static final long CONNECTION_TIMEOUT = 10 * 1000L;

	private static final long IDLE_TIMEOUT = 10 * 60 * 1000L;

	private static final long MAX_LIFE_TIME = 30 * 60 * 1000L;

	@Autowired
	private ComponentConstants componentConstants;
	
	@Bean
	public DataSource dataSource() {
		HikariDataSource dataSource = dataSourceBuilder();
		dataSource.setConnectionTimeout(CONNECTION_TIMEOUT);
		dataSource.setIdleTimeout(IDLE_TIMEOUT);
		dataSource.setMaxLifetime(MAX_LIFE_TIME);
		dataSource.setMinimumIdle(DEFAULT_POOL_SIZE);
		dataSource.setMaximumPoolSize(DEFAULT_POOL_SIZE);
		dataSource.setConnectionTestQuery(TEST_QUERY);
		return dataSource;
	}

	private HikariDataSource dataSourceBuilder() {
		HikariDataSource dataSource = null;
		if(componentConstants.getMysql().isEnable()) {
			dataSource = new HikariDataSource();
			dataSource.setDriverClassName(MysqlConfig.DRIVER_CLASS);
			dataSource.setJdbcUrl(componentConstants.getMysql().getUrl());
			dataSource.setUsername(componentConstants.getMysql().getUser());
			dataSource.setPassword(componentConstants.getMysql().getPassword());
		}else if(componentConstants.isH2Enable()) {
			//由于h2使用的MV_STORE存储引擎，当进行很多事务操作时，占用空间超大，并且不能释放空间，
			//这里不再使用h2作为默认的存储方式
			dataSource = new DefaultHikariDataSource();
			dataSource.setDriverClassName("org.h2.Driver");
			dataSource.setJdbcUrl("jdbc:h2:tcp://localhost:59539/"
				+ componentConstants.getDataPath() + "db/dhorse");
			dataSource.setUsername("dhorse");
			dataSource.setPassword("dhorse");
		}else {
			File dbPath = new File(componentConstants.getDataPath() + "db");
			if(!dbPath.exists() && !dbPath.mkdirs()){
				logger.error("Failed to create db path");
				return null;
			}
			dataSource = new HikariDataSource();
			SQLiteConfig config = new SQLiteConfig();
			config.setDateClass(DateClass.TEXT.getValue());
			config.setDateStringFormat(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
			SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
			sqLiteDataSource.setConfig(config);
			sqLiteDataSource.setUrl("jdbc:sqlite:" + componentConstants.getDataPath()
				+ "db/dhorse.db");
			dataSource.setDataSource(sqLiteDataSource);
		}
		
		return dataSource;
	}
	
	private PaginationInnerInterceptor paginationInnerInterceptor() {
		DbType dbType = DbType.SQLITE;
		if(componentConstants.getMysql().isEnable()) {
			dbType = DbType.MYSQL;
		}else if(componentConstants.isH2Enable()) {
			dbType = DbType.H2;
		}
		return new PaginationInnerInterceptor(dbType);
	}
	
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource());
		sqlSessionFactory.setConfiguration(configuration());
		sqlSessionFactory.setPlugins(mybatisPlusInterceptor());
		sqlSessionFactory.setGlobalConfig(globalConfig());
		return sqlSessionFactory.getObject();
	}

	@Bean
	public MybatisConfiguration configuration() {
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setCacheEnabled(true);
		configuration.setLazyLoadingEnabled(true);
		configuration.setAggressiveLazyLoading(false);
		configuration.setMultipleResultSetsEnabled(true);
		configuration.setUseColumnLabel(true);
		configuration.setUseGeneratedKeys(false);
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setAutoMappingBehavior(AutoMappingBehavior.FULL);
		configuration.setDefaultStatementTimeout(10);
		configuration.setLogImpl(Slf4jImpl.class);
		return configuration;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource());
		return transactionManager;
	}

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dynamicTableNameInterceptor());
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        return interceptor;
    }
    
    @Bean
	public DynamicTableNameInnerInterceptor dynamicTableNameInterceptor() {
		DynamicTableNameInnerInterceptor intercepter = new DynamicTableNameInnerInterceptor();
		intercepter.setTableNameHandler((sql, tableName) -> {
			String suffix = ThreadLocalUtils.DynamicTable.get();
			if(!StringUtils.isBlank(suffix)) {
				return tableName + suffix;
			}
			return tableName;
		});
		return intercepter;
	}
	
    @Bean
    public GlobalConfig globalConfig() {
    	GlobalConfig globalConfig = new GlobalConfig();
    	globalConfig.setBanner(false);
    	return globalConfig;
    }
}