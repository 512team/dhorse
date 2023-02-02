package org.dhorse.api.enums;

public enum NginxVersionEnum {

	V123("1.23", "1.23.3"),
	V122("1.22", "1.22.1"),
	V121("1.21", "1.21.6"),
	V120("1.20", "1.20.2"),
	V119("1.19", "1.19.10"),
	V118("1.18", "1.18.0"),
	;

	private String code;

	private String value;

	private NginxVersionEnum(String code, String value) {
		this.code = code;
		this.value = value;
	}
	
	public static NginxVersionEnum getByCode(String code) {
		for(NginxVersionEnum item : NginxVersionEnum.values()) {
			if(item.getCode().equals(code)) {
				return item;
			}
		}
		return null;
	}

	public String getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}
}
