package org.dhorse.api.param.project.env;

import java.io.Serializable;

/**
 * 添加项目环境
 * 
 * @author Dahai
 */
public class ProjectEnvCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 集群编号
	 */
	private String clusterId;

	/**
	 * 部署命名空间名称
	 */
	private String namespaceName;

	/**
	 * 环境名称，如：开发、测试、预发、生产等
	 */
	private String envName;

	/**
	 * 环境标识，如：dev、qa、pl、ol等
	 */
	private String tag;

	/**
	 * 部署顺序（值越小越往前）
	 */
	private Integer orders;

	/**
	 * 最小副本数
	 */
	private Integer minReplicas;

	/**
	 * 最大副本数
	 */
	private Integer maxReplicas;

	/**
	 * 每个副本的cpu核心数
	 */
	private Integer replicaCpu;

	/**
	 * 每个副本的内存大小，单位m
	 */
	private Integer replicaMemory;

	/**
	 * 自动扩容，cpu使用率
	 */
	private Integer autoScalingCpu;

	/**
	 * 自动扩容，内存使用率
	 */
	private Integer autoScalingMemory;

	/**
	 * 是否需要部署审批，0：否，1：是
	 */
	private Integer requiredDeployApproval;

	/**
	 * 是否需要合并代码，0：否，1：是
	 */
	private Integer requiredMerge;

	/**
	 * 链路追踪状态
	 */
	private Integer traceStatus;

	/**
	 * 链路追踪模板编号
	 */
	private String traceTemplateId;

	/**
	 * 服务端口
	 */
	private Integer servicePort;

	/**
	 * 健康检查路径，端口后的uri，如：/health
	 */
	private String healthPath;

	/**
	 * jvm参数
	 */
	private String jvmArgs;

	/**
	 * 环境描述
	 */
	private String description;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public void setOrders(Integer orders) {
		this.orders = orders;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public Integer getOrders() {
		return orders;
	}

	public Integer getMinReplicas() {
		return minReplicas;
	}

	public void setMinReplicas(Integer minReplicas) {
		this.minReplicas = minReplicas;
	}

	public Integer getMaxReplicas() {
		return maxReplicas;
	}

	public void setMaxReplicas(Integer maxReplicas) {
		this.maxReplicas = maxReplicas;
	}

	public Integer getAutoScalingCpu() {
		return autoScalingCpu;
	}

	public void setAutoScalingCpu(Integer autoScalingCpu) {
		this.autoScalingCpu = autoScalingCpu;
	}

	public Integer getAutoScalingMemory() {
		return autoScalingMemory;
	}

	public void setAutoScalingMemory(Integer autoScalingMemory) {
		this.autoScalingMemory = autoScalingMemory;
	}

	public Integer getReplicaCpu() {
		return replicaCpu;
	}

	public void setReplicaCpu(Integer replicaCpu) {
		this.replicaCpu = replicaCpu;
	}

	public Integer getReplicaMemory() {
		return replicaMemory;
	}

	public void setReplicaMemory(Integer replicaMemory) {
		this.replicaMemory = replicaMemory;
	}

	public Integer getRequiredDeployApproval() {
		return requiredDeployApproval;
	}

	public void setRequiredDeployApproval(Integer requiredDeployApproval) {
		this.requiredDeployApproval = requiredDeployApproval;
	}

	public Integer getRequiredMerge() {
		return requiredMerge;
	}

	public void setRequiredMerge(Integer requiredMerge) {
		this.requiredMerge = requiredMerge;
	}

	public Integer getTraceStatus() {
		return traceStatus;
	}

	public void setTraceStatus(Integer traceStatus) {
		this.traceStatus = traceStatus;
	}

	public String getTraceTemplateId() {
		return traceTemplateId;
	}

	public void setTraceTemplateId(String traceTemplateId) {
		this.traceTemplateId = traceTemplateId;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public Integer getServicePort() {
		return servicePort;
	}

	public void setServicePort(Integer servicePort) {
		this.servicePort = servicePort;
	}

	public String getHealthPath() {
		return healthPath;
	}

	public void setHealthPath(String healthPath) {
		this.healthPath = healthPath;
	}

	public String getJvmArgs() {
		return jvmArgs;
	}

	public void setJvmArgs(String jvmArgs) {
		this.jvmArgs = jvmArgs;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}