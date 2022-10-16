package org.dhorse.infrastructure.param;

public class ProjectExtendJavaParam extends ProjectExtendParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 打包构建方式，1：maven，2：gradle
	 */
	private Integer packageBuildType;

	/**
	 * 打包文件类型，1：jar，2：war，3：zip
	 */
	private Integer packageFileType;

	/**
	 * 打包相对路径
	 */
	private String packageTargetPath;

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

}