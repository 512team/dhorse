package org.dhorse.api.param.app.branch.deploy;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询分支部署记录
 * 
 * @author Dahai
 */
public class DeploymentDetailPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 环境编号
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