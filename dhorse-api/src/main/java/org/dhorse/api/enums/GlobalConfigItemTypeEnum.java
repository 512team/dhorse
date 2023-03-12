package org.dhorse.api.enums;

public enum GlobalConfigItemTypeEnum {

	LDAP(1, "Ldap"),
	CODEREPO(2, "代码仓库"),
	IMAGEREPO(3, "镜像仓库"),
	MAVEN(4, "Maven"),
	TRACE_TEMPLATE(5, "链路追踪模板"),
	ENV_TEMPLATE(6, "环境模板"),
	MORE(100, "更多"),
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
