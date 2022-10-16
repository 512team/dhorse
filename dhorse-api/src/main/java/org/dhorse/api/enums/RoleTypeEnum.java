package org.dhorse.api.enums;

/**
 *  用户角色类型
 * @author Dahai
 *  2021-12-4 20:20:25
 */
public enum RoleTypeEnum {

	NORMAL(0, "普通用户"), ADMIN(1, "管理员");

	private Integer code;

	private String value;

	private RoleTypeEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}

	public static RoleTypeEnum getByCode(Integer code) {
		if(code == null) {
			return null;
		}
		for(RoleTypeEnum item : RoleTypeEnum.values()) {
			if(item.getCode().equals(code)) {
				return item;
			}
		}
		return null;
	}
	
	public Integer getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}
}
