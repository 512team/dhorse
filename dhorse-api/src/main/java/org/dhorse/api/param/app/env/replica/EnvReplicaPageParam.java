package org.dhorse.api.param.app.env.replica;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询环境副本参数模型
 * 
 * @author Dahai
 */
public class EnvReplicaPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 应用环境编号
	 */
	private String appEnvId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppEnvId() {
		return appEnvId;
	}

	public void setAppEnvId(String appEnvId) {
		this.appEnvId = appEnvId;
	}

}