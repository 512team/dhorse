package org.dhorse.api.enums;

public enum PackageFileTypeTypeEnum {

	JAR(1, "jar"),
	WAR(2, "war"),
	ZIP(3, "zip"),
	;

	private Integer code;

	private String value;

	private PackageFileTypeTypeEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}
	
	public static PackageFileTypeTypeEnum getByCode(Integer code) {
		for(PackageFileTypeTypeEnum item : PackageFileTypeTypeEnum.values()) {
			if(item.getCode() == code) {
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
