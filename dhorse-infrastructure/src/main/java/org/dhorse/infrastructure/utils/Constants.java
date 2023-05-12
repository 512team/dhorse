package org.dhorse.infrastructure.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dhorse.api.enums.AppMemberRoleTypeEnum;
import org.dhorse.infrastructure.component.SpringBootApplicationHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Constants {

	private static final Logger logger = LoggerFactory.getLogger(Constants.class);
	
	/**
	 * 24小时
	 */
	public static final long HOUR_24 = 24 * 60 * 60 * 1000;
	
	public static final long ONE_MB = 1024 * 1024;
	
	public static final String MB_UNIT = "MB";
	
	public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
	
	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	
	public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMdd_HHmmss";
	
	public static final int DEPLOYED_LOG_EXIST_DAYS = 14;
	
	public static final String USR_LOCAL_HOME = "/usr/local/";
	
	public static final String AGENT_VOLUME_PATH = "/usr/local/agent/";
	
	public static final String NODE_VOLUME_PATH = "/usr/share/nginx/html";
	
	public static final String AGENT_VOLUME_NAME = "agent-volume";
	
	public static final String WAR_VOLUME_NAME = "war-volume";
	
	public static final String NODE_VOLUME_NAME = "node-volume";
	
	public static final String DHORSE_TAG = "dhorse";
	
	public static final String IMAGE_NAME_JDK = "jdk";
	
	public static final String IMAGE_NAME_TOMCAT = "tomcat";
	
	public static final String COLLECT_METRICS_URI = "/app/env/replica/metrics/add";
	
	/**
	 * 部署目录
	 */
	public static final String DHORSE_HOME = new SpringBootApplicationHome(Constants.class)
			.getSource()
			.getParentFile()
			.getParentFile()
			.getParent();
	
	/**
	 * 默认数据路径
	 */
	public static final String DEFAULT_DATA_PATH = DHORSE_HOME + "/data/";
	
	/**
	 * 默认日志路径
	 */
	public static final String DEFAULT_LOG_PATH = DHORSE_HOME + "/log/";
	
	/**
	 * 配置文件路径
	 */
	public static final String CONF_PATH = DHORSE_HOME + "/conf/";
	
	/**
	 * 临时目录相对路径
	 */
	public static final String RELATIVE_TMP_PATH = "tmp/";
	
	/**
	 * 可以操作应用成员的角色
	 */
	public static final List<Integer> ROLE_OF_OPERATE_APP_USER = Arrays.asList(
			AppMemberRoleTypeEnum.ADMIN.getCode(),
			AppMemberRoleTypeEnum.ARCHITECT.getCode(),
			AppMemberRoleTypeEnum.CHECKER.getCode());
	
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
	
	public static final String hostIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.error("Failed to get localhost ip", e);
		}
		return null;
	}
}
