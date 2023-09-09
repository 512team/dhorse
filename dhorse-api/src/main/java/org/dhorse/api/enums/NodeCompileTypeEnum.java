package org.dhorse.api.enums;

public enum NodeCompileTypeEnum {

    NPM(1, "npm"),
    PNPM(2, "pnpm"),
    YARN(3, "yarn");

    private Integer code;

    private String value;

    private NodeCompileTypeEnum(Integer code, String value) {
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
