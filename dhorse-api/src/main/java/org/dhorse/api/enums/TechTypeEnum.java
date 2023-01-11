package org.dhorse.api.enums;

public enum TechTypeEnum {

	SPRING_BOOT(1, "SpringBoot"),
	NODE(2, "Node"),
	;
	
	private Integer code;

	private String value;

	private TechTypeEnum(Integer code, String value) {
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
