package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 搜索用户的参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class UserSearchParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户名
	 */
	private String loginName;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

}