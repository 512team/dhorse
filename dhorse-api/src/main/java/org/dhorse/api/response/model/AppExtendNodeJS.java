package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendNodeJS extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * node版本
	 */
	private String nodeVersion;

	public String getNodeVersion() {
		return nodeVersion;
	}

	public void setNodeVersion(String nodeVersion) {
		this.nodeVersion = nodeVersion;
	}
}