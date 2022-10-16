package org.dhorse.api.param.cluster;

/**
 * 修改集群的参数模型。
 * 
 * @author Dahai 2021-12-01
 */
public class ClusterUpdateParam extends ClusterCreationParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群编号
	 */
	private String clusterId;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

}