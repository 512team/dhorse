package org.dhorse.api.param.cluster;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询集群的参数模型。
 * 
 * @author Dahai 2021-12-01
 */
public class ClusterPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 集群名称，如：开发、测试、预发、生产等
	 */
	private String clusterName;

	/**
	 * 集群类型，1：K8S，2：阿里云，3：腾讯云
	 */
	private Integer clusterType;

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Integer getClusterType() {
		return clusterType;
	}

	public void setClusterType(Integer clusterType) {
		this.clusterType = clusterType;
	}
}