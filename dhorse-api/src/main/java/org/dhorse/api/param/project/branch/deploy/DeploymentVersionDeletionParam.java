package org.dhorse.api.param.project.branch.deploy;

import java.io.Serializable;

/**
 * 部署版本删除参数
 * 
 * @author Dahai
 */
public class DeploymentVersionDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 部署版本编号
	 */
	private String deploymentVersionId;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDeploymentVersionId() {
		return deploymentVersionId;
	}

	public void setDeploymentVersionId(String deploymentVersionId) {
		this.deploymentVersionId = deploymentVersionId;
	}

}