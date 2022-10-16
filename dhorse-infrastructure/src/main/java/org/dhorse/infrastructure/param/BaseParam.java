package org.dhorse.infrastructure.param;

import java.io.Serializable;
import java.util.List;

public abstract class BaseParam implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String id;

	/**
	 * 项目编号
	 */
	protected String projectId;

	protected List<String> ids;

	private Integer deletionStatus;

	public Integer getDeletionStatus() {
		return deletionStatus;
	}

	public void setDeletionStatus(Integer deletionStatus) {
		this.deletionStatus = deletionStatus;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

}