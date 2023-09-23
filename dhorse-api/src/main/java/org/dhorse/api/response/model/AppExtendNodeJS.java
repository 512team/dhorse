package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendNodeJS extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * Node版本
	 */
	private String nodeVersion;

	/**
	 * Node镜像
	 */
	private String nodeImage;

	public String getNodeVersion() {
		return nodeVersion;
	}

	public void setNodeVersion(String nodeVersion) {
		this.nodeVersion = nodeVersion;
	}

	public String getNodeImage() {
		return nodeImage;
	}

	public void setNodeImage(String nodeImage) {
		this.nodeImage = nodeImage;
	}

}