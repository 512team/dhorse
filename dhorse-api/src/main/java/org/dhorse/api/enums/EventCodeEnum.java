package org.dhorse.api.enums;

/**
 * 事件类型
 * @author Dahi
 */
public enum EventCodeEnum {

	BUILD_VERSION("6001", "构建版本"),
	DEPLOY_ENV("6002", "部署环境"),
	;

	private String code;

	private String value;

	private EventCodeEnum(String code, String value) {
		this.code = code;
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}
}
