package org.dhorse.api.param.user;

import java.io.Serializable;

/**
 * 修改角色的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class RoleUpdateParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录名
	 */
	private String loginName;

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

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

}