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
	 * 技术类型，1：SpringBoot，2：Node
	 */
	private Integer techType;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getTechType() {
		return techType;
	}

	public void setTechType(Integer techType) {
		this.techType = techType;
	}

}