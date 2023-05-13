package org.dhorse.api.response.model;

import java.util.List;

public class Cluster extends BaseDto {

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
	 * 日志收集开关，0：关闭，1：开启
	 */
	private Integer logSwitch;

	/**
	 * 命名空间
	 */
	private List<String> namespaces;

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

	public String getClusterUrl() {
		return clusterUrl;
	}

	public void setClusterUrl(String clusterUrl) {
		this.clusterUrl = clusterUrl;
	}

	public Integer getAuthType() {
		return authType;
	}

	public void setAuthType(Integer authType) {
		this.authType = authType;
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

	public Integer getLogSwitch() {
		return logSwitch;
	}

	public void setLogSwitch(Integer logSwitch) {
		this.logSwitch = logSwitch;
	}

	public List<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}