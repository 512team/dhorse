package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 副本指标
 * 
 * @author Dahai 2023-02-28
 */
@TableName("REPLICA_METRICS")
public class ReplicaMetricsPO extends BaseAppPO {

	private static final long serialVersionUID = 1L;

	/**
	 * 副本名称
	 */
	private String replicaName;

	/**
	 * 指标类型，1：cpu，2：内存
	 */
	private int metricsType;

	/**
	 * 最小值
	 */
	private Long minValue;

	/**
	 * 最大值
	 */
	private Long maxValue;

	/**
	 * 当前值
	 */
	private Long currentValue;

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public int getMetricsType() {
		return metricsType;
	}

	public void setMetricsType(int metricsType) {
		this.metricsType = metricsType;
	}

	public Long getMinValue() {
		return minValue;
	}

	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}

	public Long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}

	public Long getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Long currentValue) {
		this.currentValue = currentValue;
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
