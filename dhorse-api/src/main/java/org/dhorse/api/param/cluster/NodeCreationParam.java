package org.dhorse.api.param.cluster;

import java.io.Serializable;

/**
 * 添加节点的参数模型。
 * 
 * @author Dahai 2023-11-22
 */
public class NodeCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 服务器集群编号
	 */
	private String clusterId;

	/**
	 * 节点名称
	 */
	private String nodeName;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

}