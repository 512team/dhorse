package org.dhorse.infrastructure.param;

import java.io.Serializable;

/**
 * 全局配置查询参数
 * 
 * @author Dahai 2021-09-08
 */
public class GlobalConfigQueryParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 配置项类型，1：ldap，2：代码仓库，3：镜像仓库，4：maven，5：链路追踪模板，6：环境资源模板
	 */
	private Integer itemType;

	public Integer getItemType() {
		return itemType;
	}

	public void setItemType(Integer itemType) {
		this.itemType = itemType;
	}

}
