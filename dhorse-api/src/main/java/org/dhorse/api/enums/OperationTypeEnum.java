package org.dhorse.api.enums;

public enum OperationTypeEnum {

	RESTART(1, "重启"),
	ROLLBACK(2, "回滚"),
	SCALE(3, "扩容"),
	;

	private Integer code;

	private String value;

	private OperationTypeEnum(Integer code, String value) {
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
