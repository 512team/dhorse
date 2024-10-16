package org.dhorse.infrastructure.param;

/**
 * 环境扩展参数模型
 * 
 * @author Dahai
 */
public class EnvExtParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 扩展类型，1：技术类型，2：亲和容忍，3：健康检查
	 */
	private Integer exType;

	/**
	 * 扩展内容
	 */
	private String ext;

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

	public Integer getExType() {
		return exType;
	}

	public void setExType(Integer exType) {
		this.exType = exType;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

}