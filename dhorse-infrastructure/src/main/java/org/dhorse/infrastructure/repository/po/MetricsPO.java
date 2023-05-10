package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 指标
 * 
 * @author Dahai 2023-02-28
 */
@TableName("METRICS")
public class MetricsPO extends BasePO {

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
	public List<String> getIds() {
		return this.ids;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Integer getDeletionStatus() {
		return this.deletionStatus;
	}

}
