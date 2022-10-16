package org.dhorse.api.param.project;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询项目信息参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectPageParam  extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目名称
	 */
	private String projectName;

	/**
	 * 开发语言类型，1：java，2：python，3：node，4：app，5：h5，6：c，7：c，8：go
	 */
	private Integer languageType;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getLanguageType() {
		return languageType;
	}

	public void setLanguageType(Integer languageType) {
		this.languageType = languageType;
	}

}