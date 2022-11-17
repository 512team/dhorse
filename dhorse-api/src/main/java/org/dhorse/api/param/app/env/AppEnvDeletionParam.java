package org.dhorse.api.param.app.env;

import java.io.Serializable;

/**
 * 删除应用环境
 * 
 * @author Dahai
 */
public class AppEnvDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用环境编号
	 */
	private String appEnvId;

	/**
	 * 应用编号
	 */
	private String appId;

	public String getAppEnvId() {
		return appEnvId;
	}

	public void setAppEnvId(String appEnvId) {
		this.appEnvId = appEnvId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

}