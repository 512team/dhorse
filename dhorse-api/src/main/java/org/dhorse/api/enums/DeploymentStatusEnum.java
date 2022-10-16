package org.dhorse.api.enums;

public enum DeploymentStatusEnum {

	DEPLOYING_APPROVAL(0, "部署待审批"),
	DEPLOYING(1, "部署中"),
	DEPLOYED_SUCCESS(2, "部署成功"),
	DEPLOYED_FAILURE(3, "部署失败"),
	MERGED_SUCCESS(4, "合并成功"),
	MERGED_FAILURE(5, "合并失败"),
	ROLLBACK_APPROVAL(6, "回滚待审批"),
	ROLLBACKING(7, "回滚中"),
	ROLLBACK_SUCCESS(8, "回滚成功"),
	ROLLBACK_FAILURE(9, "回滚失败"),
	;

	private Integer code;

	private String value;

	private DeploymentStatusEnum(Integer code, String value) {
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
