package org.dhorse.infrastructure.param;

import java.util.List;

/**
 * 全局配置参数
 * 
 * @author Dahai 2021-09-08
 */
public class GlobalConfigParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 配置项类型，1：ldap，2：代码仓库，3：镜像仓库，4：maven，5：链路追踪模板，6：环境资源模板
	 */
	private Integer itemType;

	private List<Integer> itemTypes;

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

	public Integer getItemType() {
		return itemType;
	}

	public void setItemType(Integer itemType) {
		this.itemType = itemType;
	}

	public List<Integer> getItemTypes() {
		return itemTypes;
	}

	public void setItemTypes(List<Integer> itemTypes) {
		this.itemTypes = itemTypes;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
