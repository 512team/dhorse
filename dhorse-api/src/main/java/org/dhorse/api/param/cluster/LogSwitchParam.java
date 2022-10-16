package org.dhorse.api.param.cluster;

import java.io.Serializable;

/**
 * 日志收集的参数模型
 * 
 * @author Dahai 2021-12-01
 */
public class LogSwitchParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 服务器集群id
	 */
	private String clusterId;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

}