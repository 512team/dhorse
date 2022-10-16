package org.dhorse.api.param.cluster;

import java.io.Serializable;

/**
 * 删除集群的参数模型。
 * 
 * @author Dahai 2021-12-01
 */
public class ClusterDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群编码
	 */
	private String clusterId;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

}