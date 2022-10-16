package org.dhorse.api.param.project.branch.deploy;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询分支部署记录
 * 
 * @author Dahai
 */
public class DeploymentDetailPageParam extends PageParam {

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