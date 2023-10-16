package org.dhorse.api.response.model;

/**
 * 集群节点
 */
public class ClusterNode extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 节点名称
	 */
	private String nodeName;

	/**
	 * 节点IP
	 */
	private String nodeIp;

	/**
	 * 状态
	 */
	private String status;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}