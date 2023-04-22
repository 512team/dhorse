package org.dhorse.api.enums;

public enum EnvExtTypeEnum {

	TEC_TYPE(1, "技术类型"),
	AFFINITY(2, "亲和容忍"),
	HEALTH(3, "健康检查"),
	;

	private Integer code;

	private String value;

	private EnvExtTypeEnum(Integer code, String value) {
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
