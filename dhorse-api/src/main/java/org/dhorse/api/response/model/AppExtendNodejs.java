package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendNodejs extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * Node版本
	 */
	private String nodeVersion;

	/**
	 * Node镜像
	 */
	private String nodeImage;
	
	/**
	 * 启动文件
	 */
	private String startFile;

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

	public String getStartFile() {
		return startFile;
	}

	public void setStartFile(String startFile) {
		this.startFile = startFile;
	}

}