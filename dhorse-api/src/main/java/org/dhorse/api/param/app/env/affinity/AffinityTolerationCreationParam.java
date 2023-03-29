package org.dhorse.api.param.app.env.affinity;

import java.io.Serializable;

/**
 * 添加亲和容忍配置参数模型
 * 
 * @author Dahai 2023-03-22
 */
public class AffinityTolerationCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 名称
	 */
	private String customizedName;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 调度类型，1：节点亲和，2：节点容忍，3：副本亲和，4：副本反亲和
	 */
	private Integer schedulingType;

	/**
	 * 亲和程度，1：硬亲和，2：软亲和
	 */
	private Integer affinityLevel;

	/**
	 * 权重值
	 */
	private String weight;

	/**
	 * 键
	 */
	private String keyName;

	/**
	 * 操作符，值：Equal、In、NotIn、Exists、DoesNotExist、Gt、Lt
	 */
	private String operator;

	/**
	 * 值，格式：值1，值2
	 */
	private String valueList;

	/**
	 * 拓扑域，如：kubernetes.io/hostname
	 */
	private String topologyKey;

	/**
	 * 作用类型，值：NoSchedule、PreferNoSchedule、NoExecute
	 */
	private String effectType;

	/**
	 * 容忍时长，单位：秒
	 */
	private String duration;

	/**
	 * 备注
	 */
	private String remark;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public Integer getSchedulingType() {
		return schedulingType;
	}

	public void setSchedulingType(Integer schedulingType) {
		this.schedulingType = schedulingType;
	}

	public String getCustomizedName() {
		return customizedName;
	}

	public void setCustomizedName(String customizedName) {
		this.customizedName = customizedName;
	}

	public String getTopologyKey() {
		return topologyKey;
	}

	public void setTopologyKey(String topologyKey) {
		this.topologyKey = topologyKey;
	}

	public Integer getAffinityLevel() {
		return affinityLevel;
	}

	public void setAffinityLevel(Integer affinityLevel) {
		this.affinityLevel = affinityLevel;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValueList() {
		return valueList;
	}

	public void setValueList(String valueList) {
		this.valueList = valueList;
	}

	public String getEffectType() {
		return effectType;
	}

	public void setEffectType(String effectType) {
		this.effectType = effectType;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}