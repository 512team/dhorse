package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 重置密码的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class PasswordSetParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 被修改用户的登录名
	 */
	private String loginName;

	/**
	 * 新登录密码，32位大写MD5值
	 */
	private String password;

	/**
	 * 确认新登录密码，32位大写MD5值
	 */
	private String confirmPassword;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
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

}