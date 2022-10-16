package org.dhorse.api.param.project.branch;

import java.io.Serializable;

/**
 * 提交部署的参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class DeploymentApplicationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 部署版本
	 */
	private String versionName;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}
}