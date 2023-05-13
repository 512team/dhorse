package org.dhorse.api.response.model;

import java.util.List;

/**
 * 应用相关用户
 * 
 * @author Dahai 2021-09-08
 */
public class AppMember extends BaseDto {

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
	 * 用户名
	 */
	private String userName;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 角色类型，1：管理员，2：开发，3：测试，4：运维，5：架构师，6：告警接收：7：部署审批
	 */
	private List<Integer> roleTypes;

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public List<Integer> getRoleTypes() {
		return roleTypes;
	}

	public void setRoleTypes(List<Integer> roleTypes) {
		this.roleTypes = roleTypes;
	}

}