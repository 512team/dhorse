package org.dhorse.api.param.app.env;

import java.io.Serializable;

/**
 * 查询生命周期配置参数模型
 * 
 * @author 无双
 */
public class EnvLifeCycleQueryParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 环境编号
	 */
	private String envId;

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

}