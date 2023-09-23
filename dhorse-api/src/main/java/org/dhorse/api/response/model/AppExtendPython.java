package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendPython extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * Python版本
	 */
	private String pythonVersion;

	/**
	 * Python镜像
	 */
	private String pythonImage;

	/**
	 * 启动文件
	 */
	private String startFile;

	public String getPythonVersion() {
		return pythonVersion;
	}

	public void setPythonVersion(String pythonVersion) {
		this.pythonVersion = pythonVersion;
	}

	public String getPythonImage() {
		return pythonImage;
	}

	public void setPythonImage(String pythonImage) {
		this.pythonImage = pythonImage;
	}

	public String getStartFile() {
		return startFile;
	}

	public void setStartFile(String startFile) {
		this.startFile = startFile;
	}

}