package org.dhorse.api.param.project.branch;

import java.io.Serializable;

/**
 * 添加项目分支
 * 
 * @author Dahai
 */
public class ProjectBranchCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 分支名
	 */
	private String branchName;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

}