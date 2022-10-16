package org.dhorse.api.param.project.member;

import java.io.Serializable;

/**
 * 删除项目成员参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectMemberDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 用户编号
	 */
	private String userId;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}