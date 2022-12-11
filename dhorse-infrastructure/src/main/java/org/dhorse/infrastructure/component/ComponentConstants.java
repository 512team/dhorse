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

	private String dataPath;

	private String logPath;

	@Autowired
	private MysqlConfig mysqlConfig;

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

	public String getVersion() {
		return version;
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

}
