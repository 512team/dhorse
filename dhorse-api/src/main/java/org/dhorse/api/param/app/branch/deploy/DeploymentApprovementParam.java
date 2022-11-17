package org.dhorse.api.param.app.branch.deploy;

import java.io.Serializable;

/**
 * 审批部署的参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class DeploymentApprovementParam implements Serializable {

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