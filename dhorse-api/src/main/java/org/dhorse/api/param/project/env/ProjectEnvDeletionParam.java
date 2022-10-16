package org.dhorse.api.param.project.env;

import java.io.Serializable;

/**
 * 删除项目环境
 * 
 * @author Dahai
 */
public class ProjectEnvDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目环境编号
	 */
	private String projectEnvId;

	/**
	 * 项目编号
	 */
	private String projectId;

	public String getProjectEnvId() {
		return projectEnvId;
	}

	public void setProjectEnvId(String projectEnvId) {
		this.projectEnvId = projectEnvId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

}