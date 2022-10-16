package org.dhorse.infrastructure.strategy.login.param;

import java.util.Date;

import org.dhorse.infrastructure.param.SysUserParam;

/**
 * 登录参数
 * 
 * @author Dahai 2021-12-01
 */
public class LoginUserParam extends SysUserParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 最后一次登录时间
	 */
	private Date lastLoginTime;

	/**
	 * 最后一次登录token
	 */
	private String lastLoginToken;

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginToken() {
		return lastLoginToken;
	}

	public void setLastLoginToken(String lastLoginToken) {
		this.lastLoginToken = lastLoginToken;
	}

}