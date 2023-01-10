package org.dhorse.api.enums;

public enum LanguageTypeEnum {

	JAVA(1, "Java"),
	NODE(2, "Node"),
	;

	private Integer code;

	private String value;

	private LanguageTypeEnum(Integer code, String value) {
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
