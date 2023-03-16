package org.dhorse.api.param.app.tag;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询应用标签
 * 
 * @author 无双
 */
public class AppTagPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 分支名称
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