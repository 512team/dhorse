package org.dhorse.api.param.app.env.affinity;

import org.dhorse.api.param.PageParam;

/**
 * 添加亲和容忍配置参数模型
 * 
 * @author Dahai 2023-03-22
 */
public class AffinityTolerationPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 调度类型，1：节点亲和，2：节点容忍，3：副本亲和，4：副本反亲和
	 */
	private Integer schedulingType;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public Integer getSchedulingType() {
		return schedulingType;
	}

	public void setSchedulingType(Integer schedulingType) {
		this.schedulingType = schedulingType;
	}
}