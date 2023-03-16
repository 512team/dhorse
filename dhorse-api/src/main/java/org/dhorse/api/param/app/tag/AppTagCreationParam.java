package org.dhorse.api.param.app.tag;

import org.dhorse.api.param.app.branch.AppBranchCreationParam;

/**
 * 添加应用标签
 * 
 * @author 无双
 */
public class AppTagCreationParam extends AppBranchCreationParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 标签名称
	 */
	private String tagName;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
}