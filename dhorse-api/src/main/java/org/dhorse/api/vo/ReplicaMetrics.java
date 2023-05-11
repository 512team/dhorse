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
	 * 指标类型，见：MetricsTypeEnum
	 */
	private Integer metricsType;

	/**
	 * 指标值
	 */
	private List<Long> metricsValues;

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

	public List<Long> getMetricsValues() {
		return metricsValues;
	}

	public void setMetricsValues(List<Long> metricsValues) {
		this.metricsValues = metricsValues;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

}
