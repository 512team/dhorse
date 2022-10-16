package org.dhorse.api.param.project;

/**
 * 修改项目参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectUpdateParam extends ProjectCreationParam {

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