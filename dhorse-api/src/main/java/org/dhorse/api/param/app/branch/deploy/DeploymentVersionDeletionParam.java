package org.dhorse.api.param.app.branch.deploy;

import java.io.Serializable;

/**
 * 部署版本删除参数
 * 
 * @author Dahai
 */
public class DeploymentVersionDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 部署版本编号
	 */
	private String deploymentVersionId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDeploymentVersionId() {
		return deploymentVersionId;
	}

	public void setDeploymentVersionId(String deploymentVersionId) {
		this.deploymentVersionId = deploymentVersionId;
	}

}