package org.dhorse.api.response.model;

import java.io.Serializable;
import java.util.List;

/**
 * 指标视图
 * 
 * @author Dahai 2023-02-28
 */
public class MetricsView implements Serializable {

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
	 * 第一类别名称
	 */
	private String firstTypeName;

	/**
	 * 第二类别名称
	 */
	private String secondeTypeName;

	/**
	 * 单位
	 */
	private String unit;

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

	public String getFirstTypeName() {
		return firstTypeName;
	}

	public void setFirstTypeName(String firstTypeName) {
		this.firstTypeName = firstTypeName;
	}

	public String getSecondeTypeName() {
		return secondeTypeName;
	}

	public void setSecondeTypeName(String secondeTypeName) {
		this.secondeTypeName = secondeTypeName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
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
