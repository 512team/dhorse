package org.dhorse.api.param.project.member;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询项目成员参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectMemberPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 登录名
	 */
	private String loginName;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

}