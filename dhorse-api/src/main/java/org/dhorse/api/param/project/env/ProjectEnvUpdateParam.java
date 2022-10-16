package org.dhorse.api.param.project.env;

/**
 * 修改项目环境
 * 
 * @author Dahai
 */
public class ProjectEnvUpdateParam extends ProjectEnvCreationParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目环境编号
	 */
	private String projectEnvId;

	public String getProjectEnvId() {
		return projectEnvId;
	}

	public void setProjectEnvId(String projectEnvId) {
		this.projectEnvId = projectEnvId;
	}

}