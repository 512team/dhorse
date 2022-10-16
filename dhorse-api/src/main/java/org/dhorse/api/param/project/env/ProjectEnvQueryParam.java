package org.dhorse.api.param.project.env;

import java.io.Serializable;

/**
 * 查询项目环境参数
 * 
 * @author Dahai
 */
public class ProjectEnvQueryParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 环境编号
	 */
	private String projectEnvId;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectEnvId() {
		return projectEnvId;
	}

	public void setProjectEnvId(String projectEnvId) {
		this.projectEnvId = projectEnvId;
	}

}