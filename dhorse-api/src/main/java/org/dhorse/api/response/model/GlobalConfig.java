package org.dhorse.api.response.model;

public class GlobalConfig extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 配置项值
	 */
	private String itemValue;

	/**
	 * 备注
	 */
	private String remark;

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
