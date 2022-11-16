package org.dhorse.api.enums;

public enum AgentImageSourceEnum {

	VERSION(1, "版本号"), CUSTOM(2, "自定义");

	private Integer code;

	private String value;

	private AgentImageSourceEnum(Integer code, String value) {
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
