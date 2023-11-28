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
	 * 配置项类型，见：GlobalConfigItemTypeEnum
	 */
	private Integer itemType;

	/**
	 * 配置项值
	 */
	private String itemValue;

	private Long version;

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

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
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
