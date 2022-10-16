package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 创建用户的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class UserCreationParam implements Serializable {

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
	 * 登录密码，32位大写MD5值
	 */
	private String password;

	/**
	 * 确认登录密码，32位大写MD5值
	 */
	private String confirmPassword;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
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