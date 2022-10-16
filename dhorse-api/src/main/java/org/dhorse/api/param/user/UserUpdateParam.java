package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 修改用户的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class UserUpdateParam implements Serializable {

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
	 * 邮箱
	 */
	private String email;

	/**
	 * 0：普通用户，1：管理员
	 */
	private Integer roleType;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

}