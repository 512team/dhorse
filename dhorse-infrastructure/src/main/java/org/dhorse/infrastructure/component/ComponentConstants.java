package org.dhorse.infrastructure.component;

import java.util.Objects;

import org.dhorse.infrastructure.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ComponentConstants {

	@Value("${version}")
	private String version;

	@Value("${server.port}")
	private String serverPort;

	private String dataPath;

	private String logPath;

	private boolean h2Enable = false;
	
	@Value("${thread_pool.build.core:#{null}}")
	private Integer threadPoolBuildCore;
	
	@Value("${thread_pool.build.max:#{null}}")
	private Integer threadPoolBuildMax;

	@Value("${thread_pool.deployment.core:#{null}}")
	private Integer threadPoolDeploymentCore;

	@Value("${thread_pool.deployment.max:#{null}}")
	private Integer threadPoolDeploymentMax;
	
	@Autowired
	private MysqlConfig mysqlConfig;
	
	@Value("${kubernetes-client:official}")
	private String kubernetesClient;

	@Value("${data.path:#{null}}")
	private void setDataPath(String dataPath) {
		if (Objects.isNull(dataPath)) {
			this.dataPath = Constants.DEFAULT_DATA_PATH;
		} else if (dataPath.endsWith("/")) {
			this.dataPath = dataPath + "dhorse/data/";
		} else {
			this.dataPath = dataPath + "/dhorse/data/";
		}
	}

	@Value("${log.path:#{null}}")
	private void setLogPath(String logPath) {
		if (Objects.isNull(logPath)) {
			this.logPath = Constants.DEFAULT_LOG_PATH;
		} else if (logPath.endsWith("/")) {
			this.logPath = logPath + "dhorse/";
		} else {
			this.logPath = logPath + "/dhorse/";
		}
	}

	public boolean isH2Enable() {
		return h2Enable;
	}

	public String getVersion() {
		return version;
	}

	public String getServerPort() {
		return serverPort;
	}

	public String getDataPath() {
		return dataPath;
	}

	public String getLogPath() {
		return logPath;
	}

	public MysqlConfig getMysql() {
		return mysqlConfig;
	}

	public Integer getThreadPoolBuildCore() {
		return threadPoolBuildCore;
	}

	public Integer getThreadPoolBuildMax() {
		return threadPoolBuildMax;
	}

	public Integer getThreadPoolDeploymentCore() {
		return threadPoolDeploymentCore;
	}

	public Integer getThreadPoolDeploymentMax() {
		return threadPoolDeploymentMax;
	}

	public String getKubernetesClient() {
		return kubernetesClient;
	}
}
