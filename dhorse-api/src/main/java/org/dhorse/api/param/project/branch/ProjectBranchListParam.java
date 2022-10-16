package org.dhorse.api.param.project.branch;

import java.io.Serializable;

/**
 * 查询分支列表
 * 
 * @author Dahai
 */
public class ProjectBranchListParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 分支名称
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