package org.dhorse.api.vo;

/**
 * 指标
 * 
 * @author Dahai 2023-02-28
 */
public class Metrics extends BaseDto {

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
	private Long metricsValue;

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

	@Override
	public String toString() {
		return "{\"replicaName\":\"" + replicaName + "\", \"metricsType\":" + metricsType
				+ ", \"metricsValue\":" + metricsValue + "}";
	}
}
