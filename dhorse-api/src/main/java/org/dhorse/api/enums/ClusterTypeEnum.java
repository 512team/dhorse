package org.dhorse.api.enums;

public enum ClusterTypeEnum {

	K8S(1, "k8s"),
	ALIYUN(2, "阿里云"),
	TENCENT(3, "腾讯云"),
	;

	private Integer code;

	private String value;

	private ClusterTypeEnum(Integer code, String value) {
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
