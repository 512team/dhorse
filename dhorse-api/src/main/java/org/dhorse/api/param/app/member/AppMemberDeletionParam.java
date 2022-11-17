package org.dhorse.api.param.app.member;

import java.io.Serializable;

/**
 * 删除应用成员参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class AppMemberDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 用户编号
	 */
	private String userId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}