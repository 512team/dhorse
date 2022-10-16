package org.dhorse.api.param.cluster.namespace;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询服务器集群命名空间。
 * 
 * @author Dahai 2021-11-24
 */
public class ClusterNamespacePageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群编号
	 */
	private String clusterId;

	/**
	 * 名称，如：default、qa、pl、ol等
	 */
	private String namespaceName;

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

}