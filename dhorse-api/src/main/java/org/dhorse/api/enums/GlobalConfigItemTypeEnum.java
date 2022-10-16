package org.dhorse.api.enums;

public enum GlobalConfigItemTypeEnum {

	LDAP(1, "ldap"),
	CODEREPO(2, "codeRepo"),
	IMAGEREPO(3, "imageRepo"),
	MAVEN(4, "maven"),
	TRACE_TEMPLATE(5, "traceTemplate"),
	;

	private Integer code;

	private String value;

	private GlobalConfigItemTypeEnum(Integer code, String value) {
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
