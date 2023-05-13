package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendJava extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * 构建方式，1：maven，2：gradle
	 */
	private Integer packageBuildType;

	/**
	 * 文件类型，1：jar，2：war，3：zip
	 */
	private Integer packageFileType;

	/**
	 * 文件路径
	 */
	private String packageTargetPath;

	/**
	 * Java版本
	 */
	private String javaVersion;

	public Integer getPackageBuildType() {
		return packageBuildType;
	}

	public void setPackageBuildType(Integer packageBuildType) {
		this.packageBuildType = packageBuildType;
	}

	public Integer getPackageFileType() {
		return packageFileType;
	}

	public void setPackageFileType(Integer packageFileType) {
		this.packageFileType = packageFileType;
	}

	public String getPackageTargetPath() {
		return packageTargetPath;
	}

	public void setPackageTargetPath(String packageTargetPath) {
		this.packageTargetPath = packageTargetPath;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

}