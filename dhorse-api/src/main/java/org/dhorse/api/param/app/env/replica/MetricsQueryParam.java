package org.dhorse.api.param.app.env.replica;

import java.io.Serializable;

/**
 * 指标查询参数模型
 * 
 * @author Dahai
 */
public class MetricsQueryParam implements Serializable {

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
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;

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

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

}