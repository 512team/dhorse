package org.dhorse.infrastructure.model;

import java.io.Serializable;

/**
 * 副本资源
 */
public class ReplicaMetrics implements Serializable {

	private static final long serialVersionUID = 1L;

	private String replicaName;

	private String appLabel;

	private long cpuUsed;

	private long memoryUsed;

	public ReplicaMetrics(String replicaName, String appLabel, long cpuUsed, long memoryUsed) {
		this.replicaName = replicaName;
		this.appLabel = appLabel;
		this.cpuUsed = cpuUsed;
		this.memoryUsed = memoryUsed;
	}

	public static ReplicaMetrics of(String replicaName, String appLabel, long cpuUsed, long memoryUsed) {
		return new ReplicaMetrics(replicaName, appLabel, cpuUsed, memoryUsed);
	}

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public String getAppLabel() {
		return appLabel;
	}

	public void setAppLabel(String appLabel) {
		this.appLabel = appLabel;
	}

	public long getCpuUsed() {
		return cpuUsed;
	}

	public void setCpuUsed(long cpuUsed) {
		this.cpuUsed = cpuUsed;
	}

	public long getMemoryUsed() {
		return memoryUsed;
	}

	public void setMemoryUsed(long memoryUsed) {
		this.memoryUsed = memoryUsed;
	}

}