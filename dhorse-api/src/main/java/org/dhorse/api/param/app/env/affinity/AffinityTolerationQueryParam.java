package org.dhorse.api.param.app.env.affinity;

import org.dhorse.api.param.PageParam;

/**
 * 添加亲和容忍配置参数模型
 * 
 * @author Dahai 2023-03-22
 */
public class AffinityTolerationQueryParam extends PageParam {

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
	 * 亲和容忍配置编号
	 */
	private String affinityTolerationId;

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

	public String getAffinityTolerationId() {
		return affinityTolerationId;
	}

	public void setAffinityTolerationId(String affinityTolerationId) {
		this.affinityTolerationId = affinityTolerationId;
	}

}