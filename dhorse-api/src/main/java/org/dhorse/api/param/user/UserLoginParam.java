package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 用户登录参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class UserLoginParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录来源，见：org.dhorse.api.enums.RegisteredSourceEnum
	 */
	private Integer loginSource;

	/**
	 * 登录名
	 */
	private String loginName;

	/**
	 * 登录密码，MD5值（32位大写）
	 */
	private String password;
	
	/**
	 * 企业微信登录码
	 */
	private String wechatCode;

	public Integer getLoginSource() {
		return loginSource;
	}

	public void setLoginSource(Integer loginSource) {
		this.loginSource = loginSource;
	}

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

	public String getWechatCode() {
		return wechatCode;
	}

	public void setWechatCode(String wechatCode) {
		this.wechatCode = wechatCode;
	}

}