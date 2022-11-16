package org.dhorse.api.param.cluster;

import java.io.Serializable;

/**
 * 添加集群的参数模型。
 * 
 * @author Dahai 2021-12-01
 */
public class ClusterCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群名，如：开发、测试、预发、生产等
	 */
	private String clusterName;

	/**
	 * 集群类型，1：K8S，2：阿里云，3：腾讯云
	 */
	private Integer clusterType;

	/**
	 * 集群地址，带端口
	 */
	private String clusterUrl;

	/**
	 * 认证方式，参见：AuthTypeEnum
	 */
	private Integer authType;

	/**
	 * 认证token
	 */
	private String authToken;

	/**
	 * 认证名称
	 */
	private String authName;

	/**
	 * 认证密码
	 */
	private String authPassword;

	/**
	 * 日志收集开关 <br>
	 * 枚举值：org.dhorse.api.enums.YesOrNoEnum
	 */
	private Integer logSwitch;

	/**
	 * 描述
	 */
	private String description;

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Integer getClusterType() {
		return clusterType;
	}

	public void setClusterType(Integer clusterType) {
		this.clusterType = clusterType;
	}

	public Integer getAuthType() {
		return authType;
	}

	public void setAuthType(Integer authType) {
		this.authType = authType;
	}

	public Integer getLogSwitch() {
		return logSwitch;
	}

	public void setLogSwitch(Integer logSwitch) {
		this.logSwitch = logSwitch;
	}

	public String getClusterUrl() {
		return clusterUrl;
	}

	public void setClusterUrl(String clusterUrl) {
		this.clusterUrl = clusterUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAuthName() {
		return authName;
	}

	public void setAuthName(String authName) {
		this.authName = authName;
	}

	public String getAuthPassword() {
		return authPassword;
	}

	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}