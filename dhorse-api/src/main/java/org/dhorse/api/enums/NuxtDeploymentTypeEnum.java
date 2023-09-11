package org.dhorse.api.enums;

public enum NuxtDeploymentTypeEnum {

	DYNAMIC(1, "动态部署"),
	STATIC(2, "静态部署");
	
	private Integer code;

	private String value;

	private NuxtDeploymentTypeEnum(Integer code, String value) {
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
