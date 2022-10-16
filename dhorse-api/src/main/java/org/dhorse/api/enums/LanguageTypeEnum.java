package org.dhorse.api.enums;

public enum LanguageTypeEnum {

	JAVA(1, "Java"),
	PYTHON(2, "Python"),
	NODE(3, "Node"),
	APP(4, "App"),
	H5(5, "H5"),
	C(6, "C"),
	CC(7, "C++"),
	GO(8, "Go"),
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
