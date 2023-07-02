package org.dhorse.api.response.model;

/**
 * 环境Prometheus配置
 * 
 * @author 无双
 */
public class EnvPrometheus extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 采集维度
	 */
	private String kind;

	/**
	 * 采集端口
	 */
	private String port;

	/**
	 * 采集路径
	 */
	private String path;

	/**
	 * 采集开关
	 */
	private String scrape;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getScrape() {
		return scrape;
	}

	public void setScrape(String scrape) {
		this.scrape = scrape;
	}

}