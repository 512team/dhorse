package org.dhorse.api.param.app.env;

import java.io.Serializable;

/**
 * 修改链路追踪参数模型
 * 
 * @author Dahai
 */
public class TraceUpdateParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用环境编号
	 */
	private String appEnvId;

	/**
	 * 应用编号
	 */
	private String appId;

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

	public String getAppEnvId() {
		return appEnvId;
	}

	public void setAppEnvId(String appEnvId) {
		this.appEnvId = appEnvId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Integer getTraceStatus() {
		return traceStatus;
	}

	public void setTraceStatus(Integer traceStatus) {
		this.traceStatus = traceStatus;
	}

}