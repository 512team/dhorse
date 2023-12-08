package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendDotNet extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * .NET版本
	 */
	private String dotNetVersion;

	/**
	 * .NET镜像
	 */
	private String dotNetImage;

	public String getDotNetVersion() {
		return dotNetVersion;
	}

	public void setDotNetVersion(String dotNetVersion) {
		this.dotNetVersion = dotNetVersion;
	}

	public String getDotNetImage() {
		return dotNetImage;
	}

	public void setDotNetImage(String dotNetImage) {
		this.dotNetImage = dotNetImage;
	}

}