package org.dhorse.api.enums;

public enum GlobalConfigItemTypeEnum {

	LDAP(1, "Ldap"),
	CODE_REPO(2, "代码仓库"),
	IMAGE_REPO(3, "镜像仓库"),
	MAVEN(4, "Maven"),
	TRACE_TEMPLATE(5, "链路追踪模板"),
	ENV_TEMPLATE(6, "环境模板"),
	CUSTOMIZED_MENU(7, "自义定菜单"),
	COLLECT_REPLICA_METRICS_TASK_TIME(8, "收集副本指标的时间"),
	WECHAT(9, "企业微信"),
	DING_DING(10, "钉钉登录"),
	CAS(11, "CAS登录"),
	FEI_SHU(12, "飞书登录"),
	SERVER_IP(13, "DHorse服务器IP"),
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
