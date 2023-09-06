package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendPython extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * Python版本
	 */
	private String pythonVersion;

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

	public String getStartFile() {
		return startFile;
	}

	public void setStartFile(String startFile) {
		this.startFile = startFile;
	}

}