package org.dhorse.api.enums;

/**
 * 亲和程度
 */
public enum AffinityLevelEnum {

	FORCE_AFFINITY(1, "硬亲和"),
	SOFT_AFFINITY(2, "软亲和"),
	;

	private Integer code;

	private String value;

	private AffinityLevelEnum(Integer code, String value) {
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
