package org.dhorse.api.param.project;

import java.io.Serializable;

/**
 * 查询项目参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectQueryParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

}