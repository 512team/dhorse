package org.dhorse.infrastructure.param;

import java.util.List;

/**
 * 用户表
 * 
 * @author Dahai 2021-12-01
 */
public class SysUserParam extends PageParam {

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
	 * 登录密码
	 */
	private String password;

	/**
	 * 确认登录密码
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

	/**
	 * 上次登录的token
	 */
	private String lastLoginToken;

	/**
	 * 注册来源，1：DHorse，2：LDAP，3：CAS
	 */
	private Integer registeredSource;

	private List<String> loginNames;

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

	public String getLastLoginToken() {
		return lastLoginToken;
	}

	public void setLastLoginToken(String lastLoginToken) {
		this.lastLoginToken = lastLoginToken;
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

	public Integer getRegisteredSource() {
		return registeredSource;
	}

	public void setRegisteredSource(Integer registeredSource) {
		this.registeredSource = registeredSource;
	}

	public List<String> getLoginNames() {
		return loginNames;
	}

	public void setLoginNames(List<String> loginNames) {
		this.loginNames = loginNames;
	}

}