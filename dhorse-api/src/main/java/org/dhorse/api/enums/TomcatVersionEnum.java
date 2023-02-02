package org.dhorse.api.enums;

public enum TomcatVersionEnum {

	V10(10, "10.1.4"),
	V9(9, "9.0.70"),
	V8(8, "8.5.84"),
	;

	private Integer code;

	private String value;

	private TomcatVersionEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}
	
	public static TomcatVersionEnum getByCode(Integer code) {
		for(TomcatVersionEnum item : TomcatVersionEnum.values()) {
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
