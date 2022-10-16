package org.dhorse.infrastructure.param;

public class ClusterParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群名，如：开发、测试、预发、生产等
	 */
	private String clusterName;

	/**
	 * 集群类型，1：私有k8s，2：阿里云，3：腾讯云
	 */
	private Integer clusterType;

	/**
	 * 集群地址，带端口
	 */
	private String clusterUrl;

	/**
	 * 认证token
	 */
	private String authToken;

	/**
	 * 认证用户
	 */
	private String authName;

	/**
	 * 认证密码
	 */
	private String authPassword;

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