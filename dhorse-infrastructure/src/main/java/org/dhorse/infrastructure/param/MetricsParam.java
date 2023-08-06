package org.dhorse.infrastructure.param;

/**
 * 指标
 * 
 * @author Dahai 2023-02-28
 */
public class MetricsParam extends PageParam {

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
	 * 指标值，cpu的存储单位：m，内存的存储单位：字节
	 */
	private Long metricsValue;

	private String startTime;

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

	public Long getMetricsValue() {
		return metricsValue;
	}

	public void setMetricsValue(Long metricsValue) {
		this.metricsValue = metricsValue;
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
