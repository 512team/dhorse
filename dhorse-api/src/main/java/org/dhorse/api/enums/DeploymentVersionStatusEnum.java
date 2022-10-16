package org.dhorse.api.enums;

/**
 * 部署版本状态
 * 
 * @author Dahai
 * @date 2022-8-13 17:12:50
 */
public enum DeploymentVersionStatusEnum {

	BUILDING(0, "构建中"),
	BUILDED_SUCCESS(1, "构建成功"),
	BUILDED_FAILUR(2, "构建失败");

	private Integer code;

	private String value;

	private DeploymentVersionStatusEnum(Integer code, String value) {
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
