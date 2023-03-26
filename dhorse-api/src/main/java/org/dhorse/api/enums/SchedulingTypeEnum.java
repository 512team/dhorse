package org.dhorse.api.enums;

/**
 * 调度类型
 */
public enum SchedulingTypeEnum {

	NODE_AFFINITY(1, "节点容忍"),
	NODE_TOLERATION(2, "节点容忍"),
	REPLICA_AFFINITY(3, "副本亲和"),
	REPLICA_ANTIAFFINITY(4, "副本反亲和"),
	;

	private Integer code;

	private String value;

	private SchedulingTypeEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}

	public Integer getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}
}
