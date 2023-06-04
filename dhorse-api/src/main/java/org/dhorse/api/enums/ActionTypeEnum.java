package org.dhorse.api.enums;

/**
 * 动作类型
 * @author 无双
 *
 */
public enum ActionTypeEnum {

	HTTP_GET(1, "HTTP_GET"),
	TCP(2, "TCP"),
	EXEC(3, "EXEC"),
	;

	private Integer code;

	private String value;

	private ActionTypeEnum(Integer code, String value) {
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
