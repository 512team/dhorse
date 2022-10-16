package org.dhorse.infrastructure.param;

import java.util.List;

/**
 * 项目成员参数
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectMemberParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户编号
	 */
	private String userId;

	/**
	 * 登录名
	 */
	private String loginName;

	/**
	 * 角色类型，1：管理员，2：开发，3：测试，4：运维，5：架构师，6：告警接收：7：部署审批
	 */
	private List<Integer> roleTypes;

	private List<String> projectIds;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public List<Integer> getRoleTypes() {
		return roleTypes;
	}

	public void setRoleTypes(List<Integer> roleTypes) {
		this.roleTypes = roleTypes;
	}

	public List<String> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<String> projectIds) {
		this.projectIds = projectIds;
	}

}