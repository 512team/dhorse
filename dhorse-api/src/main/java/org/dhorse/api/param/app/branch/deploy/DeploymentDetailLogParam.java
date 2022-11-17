package org.dhorse.api.param.app.branch.deploy;

import java.io.Serializable;

/**
 * 分支部署记录日志
 * 
 * @author Dahai
 */
public class DeploymentDetailLogParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 分支编号
	 */
	private String branchName;

	/**
	 * 分支部署记录编号
	 */
	private String deploymentDetailId;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getDeploymentDetailId() {
		return deploymentDetailId;
	}

	public void setDeploymentDetailId(String deploymentDetailId) {
		this.deploymentDetailId = deploymentDetailId;
	}

}