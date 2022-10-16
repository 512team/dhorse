package org.dhorse.api.param.project.branch.deploy;

import org.dhorse.api.param.PageParam;

/**
 * 版本分页参数
 * 
 * @author Dahai
 */
public class DeploymentVersionPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 分支名称
	 */
	private String branchName;

	/**
	 * 状态，0：构建中，1：构建成功，2：构建失败
	 */
	private Integer status;

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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}