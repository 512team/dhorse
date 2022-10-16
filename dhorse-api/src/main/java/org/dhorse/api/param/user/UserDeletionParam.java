package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 删除用户的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class UserDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录名
	 */
	private String loginName;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

}