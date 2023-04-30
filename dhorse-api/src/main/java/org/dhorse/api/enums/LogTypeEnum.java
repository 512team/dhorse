package org.dhorse.api.enums;

/**
 * 日志类型
 * 
 * @author Dahi
 */
public enum LogTypeEnum {

	BUILD_VERSION(1, "构建日志"),
	DEPLOY_ENV(2, "部署日志"),
	;

	private Integer code;

	private String value;

	private LogTypeEnum(Integer code, String value) {
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
