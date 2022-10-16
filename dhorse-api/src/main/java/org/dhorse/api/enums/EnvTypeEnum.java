package org.dhorse.api.enums;

public enum EnvTypeEnum {

	DEV(1, "开发环境"),
	QA(2, "测试环境"),
	PL(3, "预发环境"),
	OL(4, "生产环境"),
	;

	private Integer code;

	private String value;

	private EnvTypeEnum(Integer code, String value) {
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
