package org.dhorse.api.param.app.member;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询应用成员参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class AppMemberPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 登录名
	 */
	private String loginName;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

}