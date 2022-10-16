package org.dhorse.api.param.project.env.replica;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询环境副本参数模型
 * 
 * @author Dahai
 */
public class EnvReplicaPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 项目环境编号
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