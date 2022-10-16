package org.dhorse.api.enums;

public enum PackageBuildTypeEnum {

	MAVEN(1, "Maven"),
	GRADLE(2, "Gradle"),
	;

	private Integer code;

	private String value;

	private PackageBuildTypeEnum(Integer code, String value) {
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
