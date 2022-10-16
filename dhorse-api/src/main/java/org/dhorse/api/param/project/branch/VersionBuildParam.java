package org.dhorse.api.param.project.branch;

import java.io.Serializable;

/**
 * 构建版本参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class VersionBuildParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 分支编号
	 */
	private String branchName;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
}