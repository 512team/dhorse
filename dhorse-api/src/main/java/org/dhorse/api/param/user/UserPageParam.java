package org.dhorse.api.param.user;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询用户的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class UserPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录名
	 */
	private String loginName;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 0：普通用户，1：管理员
	 */
	private Integer roleType;

	/**
	 * 注册来源，1：DHorse，2：LDAP，3：SSO
	 */
	private Integer registeredSource;

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

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

	public Integer getRegisteredSource() {
		return registeredSource;
	}

	public void setRegisteredSource(Integer registeredSource) {
		this.registeredSource = registeredSource;
	}
}