package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 修改密码的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class PasswordUpdateParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 旧登录密码，32位大写MD5值
	 */
	private String oldPassword;

	/**
	 * 新登录密码，32位大写MD5值
	 */
	private String password;

	/**
	 * 确认新登录密码，32位大写MD5值
	 */
	private String confirmPassword;

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
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