package org.dhorse.api.param.app.branch.deploy;

import java.io.Serializable;

/**
 * 终止部署参数模型
 * 
 * @author 无双 2023-04-09
 */
public class AbortDeploymentParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 部署明细编号
	 */
	private String deploymentDetailId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDeploymentDetailId() {
		return deploymentDetailId;
	}

	public void setDeploymentDetailId(String deploymentDetailId) {
		this.deploymentDetailId = deploymentDetailId;
	}
}