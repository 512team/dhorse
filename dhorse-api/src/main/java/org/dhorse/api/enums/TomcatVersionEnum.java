package org.dhorse.api.enums;

public enum TomcatVersionEnum {

	JAR(10, "10.1.4"),
	WAR(8, "8.5.84"),
	ZIP(9, "9.0.70"),
	;

	private Integer code;

	private String value;

	private TomcatVersionEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}
	
	public static TomcatVersionEnum getByCode(Integer code) {
		for(TomcatVersionEnum item : TomcatVersionEnum.values()) {
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
