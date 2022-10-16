package org.dhorse.api.enums;

public enum YesOrNoEnum {

	NO(0, "否"), YES(1, "是");

	private Integer code;

	private String value;

	private YesOrNoEnum(Integer code, String value) {
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
