package org.dhorse.api.enums;

/**
 * 应用成员角色类型
 */
public enum AppUserRoleTypeEnum {

	ADMIN(1, "管理员"),
	DEVELOPER(2, "开发"),
	TESTER(3, "测试"),
	OPERATOR(4, "运维"),
	ARCHITECT(5, "架构师"),
	REPORTOR(6, "告警接收"),
	CHECKER(7, "部署审批"),
	;

	private Integer code;

	private String value;

	private AppUserRoleTypeEnum(Integer code, String value) {
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
