package org.dhorse.api.param.cluster;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询集群节点参数模型
 * 
 * @author Dahai
 */
public class ClusterNodePageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群编号
	 */
	private String clusterId;

	/**
	 * 节点名称
	 */
	private String nodeName;

	/**
	 * 节点IP
	 */
	private String nodeIp;

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

	public String getNodeIp() {
		return nodeIp;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

}