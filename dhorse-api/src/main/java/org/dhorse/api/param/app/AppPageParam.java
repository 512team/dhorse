package org.dhorse.api.param.app;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询应用信息参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class AppPageParam  extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 开发语言类型，1：java，2：python，3：node，4：app，5：h5，6：c，7：c，8：go
	 */
	private Integer languageType;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getLanguageType() {
		return languageType;
	}

	public void setLanguageType(Integer languageType) {
		this.languageType = languageType;
	}

}