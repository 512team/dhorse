package org.dhorse.api.param.project.branch.deploy;

import java.io.Serializable;

/**
 * 审批部署的参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class DeploymentApprovementParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 部署明细编号
	 */
	private String deploymentDetailId;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDeploymentDetailId() {
		return deploymentDetailId;
	}

	public void setDeploymentDetailId(String deploymentDetailId) {
		this.deploymentDetailId = deploymentDetailId;
	}
}