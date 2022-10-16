package org.dhorse.api.param.project.branch;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询项目分支
 * 
 * @author Dahai
 */
public class ProjectBranchPageParam extends PageParam {

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