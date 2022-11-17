package org.dhorse.infrastructure.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dhorse.api.enums.AppUserRoleTypeEnum;
import org.dhorse.infrastructure.component.SpringBootApplicationHome;


public class Constants {

	/**
	 * 登录状态有效时长（毫秒）
	 */
	public static final long LOGINED_VALID_MILLISECONDS_TIME = 24 * 60 * 60 * 1000;
	
	
	public static final String DEPLOYED_RELATIVE_PATH = "deployment/";
	
	public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
	
	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	
	public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	
	public static final int DEPLOYED_LOG_EXIST_DAYS = 14;
	
	public static final String IMAGE_REPO_APP = "dhorse";
	
	/**
	 * 部署目录
	 */
	public static final String JAR_HOME = new SpringBootApplicationHome(Constants.class)
			.getSource()
			.getParentFile()
			.getParentFile()
			.getParent();
	
	/**
	 * 默认数据路径
	 */
	public static final String DEFAULT_DATA_PATH = JAR_HOME + "/data/";
	
	/**
	 * 默认日志路径
	 */
	public static final String DEFAULT_LOG_PATH = JAR_HOME + "/log/";
	
	/**
	 * 配置文件路径
	 */
	public static final String CONF_PATH = JAR_HOME + "/conf/";
	
	/**
	 * 临时目录相对路径
	 */
	public static final String RELATIVE_TMP_PATH = "tmp/";
	
	/**
	 * 部署日志文件路径
	 */
	public static String publishedLogPath(String logPath, Date deplopyedStartTime) {
		return new StringBuilder()
    			.append(logPath)
    			.append("published/")
    			.append(new SimpleDateFormat(DATE_FORMAT_YYYYMMDD).format(deplopyedStartTime))
    			.append("/")
    			.toString();
	}
	
	/**
	 * 部署日志的文件路径
	 */
	public static String deploymentLogFile(String logPath, Date deplopyedStartTime, String fileName) {
		return new StringBuilder()
    			.append(publishedLogPath(logPath, deplopyedStartTime))
    			.append("deployment/")
    			.append(fileName)
    			.append(".log").toString();
	}
	
	/**
	 * 构建版本的日志文件路径
	 */
	public static String buildVersionLogFile(String logPath, Date deplopyedStartTime, String fileName) {
		return new StringBuilder()
    			.append(publishedLogPath(logPath, deplopyedStartTime))
    			.append("build/")
    			.append(fileName)
    			.append(".log").toString();
	}
	
	/**
	 * 可以操作应用成员的角色
	 */
	public static final List<Integer> ROLE_OF_OPERATE_APP_USER = Arrays.asList(
			AppUserRoleTypeEnum.ADMIN.getCode(),
			AppUserRoleTypeEnum.ARCHITECT.getCode(),
			AppUserRoleTypeEnum.CHECKER.getCode());
}
