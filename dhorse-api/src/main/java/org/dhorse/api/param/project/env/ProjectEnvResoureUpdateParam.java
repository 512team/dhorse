package org.dhorse.api.param.project.env;

import java.io.Serializable;

/**
 * 修改项目环境的资源
 * 
 * @author Dahai
 */
public class ProjectEnvResoureUpdateParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 项目环境编号
	 */
	private String projectEnvId;

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

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectEnvId() {
		return projectEnvId;
	}

	public void setProjectEnvId(String projectEnvId) {
		this.projectEnvId = projectEnvId;
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

}