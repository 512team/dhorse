package org.dhorse.api.enums;

public enum AuthTypeEnum {

	TOKEN(1, "令牌认证"),
	ACCOUNT(2, "账号认证"),
	;

	private Integer code;

	private String value;

	private AuthTypeEnum(Integer code, String value) {
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
