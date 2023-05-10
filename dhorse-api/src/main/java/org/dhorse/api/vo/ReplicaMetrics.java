package org.dhorse.api.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 副本指标
 * 
 * @author Dahai 2023-02-28
 */
public class ReplicaMetrics implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 副本名称
	 */
	private String replicaName;

	/**
	 * 指标类型，1：CPU（m），2：内存（MB）
	 */
	private Integer metricsType;

	/**
	 * 最大值
	 */
	private List<Long> maxValues;

	/**
	 * 使用值
	 */
	private List<Long> usedValues;

	/**
	 * 时间
	 */
	private List<String> times;

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public Integer getMetricsType() {
		return metricsType;
	}

	public void setMetricsType(Integer metricsType) {
		this.metricsType = metricsType;
	}

	public List<Long> getMaxValues() {
		return maxValues;
	}

	public void setMaxValues(List<Long> maxValues) {
		this.maxValues = maxValues;
	}

	public List<Long> getUsedValues() {
		return usedValues;
	}

	public void setUsedValues(List<Long> usedValues) {
		this.usedValues = usedValues;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

}
