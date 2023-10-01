package org.dhorse.api.response.model;

public class AppExtendNext extends AppExtendNodejs {

	private static final long serialVersionUID = 1L;

	/**
	 * Node镜像
	 */
	private String nodeImage;

	/**
	 * 部署方式，1：动态部署，2：静态部署
	 */
	private Integer deploymentType;

	public String getNodeImage() {
		return nodeImage;
	}

	public void setNodeImage(String nodeImage) {
		this.nodeImage = nodeImage;
	}

	public Integer getDeploymentType() {
		return deploymentType;
	}

	public void setDeploymentType(Integer deploymentType) {
		this.deploymentType = deploymentType;
	}

}