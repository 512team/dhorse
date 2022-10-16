package org.dhorse.api.param.project.env;

import java.io.Serializable;

/**
 * 修改链路追踪参数模型
 * 
 * @author Dahai
 */
public class TraceUpdateParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目环境编号
	 */
	private String projectEnvId;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 链路追踪状态，0：关闭，1：开启
	 */
	private Integer traceStatus;

	/**
	 * 链路追踪模板编号
	 */
	private String traceTemplateId;

	public String getTraceTemplateId() {
		return traceTemplateId;
	}

	public void setTraceTemplateId(String traceTemplateId) {
		this.traceTemplateId = traceTemplateId;
	}

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

	public Integer getTraceStatus() {
		return traceStatus;
	}

	public void setTraceStatus(Integer traceStatus) {
		this.traceStatus = traceStatus;
	}

}