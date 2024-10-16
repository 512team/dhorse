package org.dhorse.api.param.app.branch;

import java.io.Serializable;

/**
 * 查询标签列表
 * 
 * @author Dahai
 */
public class AppTagListParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 标签名称
	 */
	private String tagName;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

}