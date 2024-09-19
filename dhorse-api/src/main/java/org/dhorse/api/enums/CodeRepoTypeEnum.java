package org.dhorse.api.enums;

public enum CodeRepoTypeEnum {

	GITLAB(1, "GitLab"),
	GITHUB(2, "GitHub"),
	GITEE(3, "Gitee"),
	CODEUP(4, "Codeup");

	private Integer code;

	private String value;

	private CodeRepoTypeEnum(Integer code, String value) {
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
