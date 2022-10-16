package org.dhorse.infrastructure.repository.po;

public abstract class BaseProjectPO extends BasePO {

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
