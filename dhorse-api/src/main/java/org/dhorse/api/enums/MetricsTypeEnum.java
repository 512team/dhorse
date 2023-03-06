package org.dhorse.api.enums;

public enum MetricsTypeEnum {

	CPU(1, "CPU"),
	MEMORY(2, "内存"),
	;

	private Integer code;

	private String value;

	private MetricsTypeEnum(Integer code, String value) {
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
