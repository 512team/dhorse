package org.dhorse.api.response.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用环境配置
 * 
 * @author Dahai 2021-09-08
 */
public class AppEnv extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 集群编号
	 */
	private String clusterId;

	/**
	 * 集群名称
	 */
	private String clusterName;

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
	 * 部署版本
	 */
	private String versionName;

	/**
	 * 部署分支
	 */
	private String branchName;

	/**
	 * 部署序号（值越小越往前）
	 */
	private Integer deploymentOrder;

	/**
	 * 最小副本数
	 */
	private Integer minReplicas;

	/**
	 * 最大副本数
	 */
	private Integer maxReplicas;

	/**
	 * 每副本CPU，单位m
	 */
	private Integer replicaCpu;

	/**
	 * 每副本内存，单位MB
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
	 * 事件通知地址，格式如：http(s)://notify_server:port/receive
	 */
	private String eventNofigyUrl;

	/**
	 * 是否需要部署审批，0：否，1：是
	 */
	private Integer requiredDeployApproval;

	/**
	 * 是否需要合并代码，0：否，1：是
	 */
	private Integer requiredMerge;

	/**
	 * 服务端口
	 */
	private Integer servicePort;

	/**
	 * 辅助端口，如：8081,8082
	 */
	private String minorPorts;

	/**
	 * 部署批次
	 */
	private Integer batchSize;

	/**
	 * 链路追踪状态，0：未开启，1：已开启
	 */
	private Integer traceStatus;

	/**
	 * 链路追踪模板编号
	 */
	private String traceTemplateId;

	/**
	 * 访问域名
	 */
	private String ingressHost;

	/**
	 * 链路追踪模板名称
	 */
	private String traceTemplateName;

	/**
	 * 环境描述
	 */
	private String description;

	/**
	 * 部署时间
	 */
	private Date deploymentTime;

	/**
	 * 环境扩展信息，分页查询时不返回数据
	 */
	private EnvExtend envExtend;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getEventNofigyUrl() {
		return eventNofigyUrl;
	}

	public void setEventNofigyUrl(String eventNofigyUrl) {
		this.eventNofigyUrl = eventNofigyUrl;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getMinorPorts() {
		return minorPorts;
	}

	public void setMinorPorts(String minorPorts) {
		this.minorPorts = minorPorts;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public Date getDeploymentTime() {
		return deploymentTime;
	}

	public void setDeploymentTime(Date deploymentTime) {
		this.deploymentTime = deploymentTime;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public void setDeploymentOrder(Integer deploymentOrder) {
		this.deploymentOrder = deploymentOrder;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public Integer getDeploymentOrder() {
		return deploymentOrder;
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

	public String getTraceTemplateId() {
		return traceTemplateId;
	}

	public void setTraceTemplateId(String traceTemplateId) {
		this.traceTemplateId = traceTemplateId;
	}

	public String getTraceTemplateName() {
		return traceTemplateName;
	}

	public void setTraceTemplateName(String traceTemplateName) {
		this.traceTemplateName = traceTemplateName;
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

	public Integer getTraceStatus() {
		return traceStatus;
	}

	public void setTraceStatus(Integer traceStatus) {
		this.traceStatus = traceStatus;
	}

	public String getIngressHost() {
		return ingressHost;
	}

	public void setIngressHost(String ingressHost) {
		this.ingressHost = ingressHost;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("unchecked")
	public <T extends EnvExtend> T getEnvExtend() {
		return (T) envExtend;
	}

	public void setEnvExtend(EnvExtend envExtend) {
		this.envExtend = envExtend;
	}

	public static abstract class EnvExtend implements Serializable {

		private static final long serialVersionUID = 1L;

	}

	public static class EnvExtendNode extends EnvExtend {

		private static final long serialVersionUID = 1L;

	}

	public static class EnvExtendSpringBoot extends EnvExtend {

		private static final long serialVersionUID = 1L;

		private Integer jvmMetricsStatus;

		private Integer traceStatus;

		private String traceTemplateId;

		private String jvmArgs;

		public Integer getJvmMetricsStatus() {
			return jvmMetricsStatus;
		}

		public void setJvmMetricsStatus(Integer jvmMetricsStatus) {
			this.jvmMetricsStatus = jvmMetricsStatus;
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

		public String getJvmArgs() {
			return jvmArgs;
		}

		public void setJvmArgs(String jvmArgs) {
			this.jvmArgs = jvmArgs;
		}

	}
}