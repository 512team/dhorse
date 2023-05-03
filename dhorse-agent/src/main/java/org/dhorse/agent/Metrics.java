package org.dhorse.agent;

import java.io.Serializable;

/**
 * 指标模型
 * 
 * @author 无双 2023-05-02
 */
public class Metrics implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 副本名称
	 */
	private String replicaName;

	/**
	 * 第一类型，见：MetricsTypeEnum
	 */
	private Integer firstType;

	/**
	 * 第二类型，见：MetricsTypeEnum
	 */
	private Integer secondType;

	/**
	 * 指标值
	 */
	private Long metricsValue;

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public Integer getFirstType() {
		return firstType;
	}

	public void setFirstType(Integer firstType) {
		this.firstType = firstType;
	}

	public Integer getSecondType() {
		return secondType;
	}

	public void setSecondType(Integer secondType) {
		this.secondType = secondType;
	}

	public Long getMetricsValue() {
		return metricsValue;
	}

	public void setMetricsValue(Long metricsValue) {
		this.metricsValue = metricsValue;
	}

	@Override
	public String toString() {
		return "{\"replicaName\":\"" + replicaName + "\", \"firstType\":" + firstType
				+ ", \"secondType\":" + secondType + ", \"metricsValue\":" + metricsValue + "}";
	}
}