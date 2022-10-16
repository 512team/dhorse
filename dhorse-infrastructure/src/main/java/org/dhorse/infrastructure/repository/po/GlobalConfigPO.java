package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 全局配置表
 * 
 * @author Dahai 2021-09-08
 */
@TableName("GLOBAL_CONFIG")
public class GlobalConfigPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 配置项类型，1：ldap，2：代码仓库，3：镜像仓库，4：maven
	 */
	private Integer itemType;

	/**
	 * 配置项值
	 */
	private String itemValue;

	@TableField(exist = false)
	private List<Integer> itemTypes;

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

	@Override
	public List<String> getIds() {
		return this.ids;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Integer getDeletionStatus() {
		return this.deletionStatus;
	}

}
