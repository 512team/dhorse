package org.dhorse.api.response.model;

import java.io.Serializable;

/**
 * 日志收集器状态模型
 * 
 * @author Dahai 2021-09-08
 */
public class LogCollectorStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 状态，0：关闭，1：开启
	 */
	private Integer status;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}