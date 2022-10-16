package org.dhorse.api.param.global;

import java.io.Serializable;

/**
 * 全局配置分页参数模型
 * 
 * @author Dahai 2022-10-14
 */
public class GlolabConfigDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 配置编号
	 */
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}