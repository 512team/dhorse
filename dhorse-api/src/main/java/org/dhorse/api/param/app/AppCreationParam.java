package org.dhorse.api.param.app;

import java.io.Serializable;

/**
 * 新增应用参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class AppCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 语言类型，1：java，2：python，3：node，4：app，5：h5，6：c，7：c，8：go
	 */
	private Integer languageType;

	/**
	 * 基础镜像来源，1：版本号，2：自定义
	 */
	private Integer baseImageSource;

	/**
	 * 基础镜像版本
	 */
	private String baseImageVersion;

	/**
	 * 基础镜像，如：openjdk:11.0.16-jdk
	 */
	private String baseImage;

	/**
	 * 代码仓库地址
	 */
	private String codeRepoPath;

	/**
	 * 一级部门
	 */
	private String firstDepartment;

	/**
	 * 二级部门
	 */
	private String secondDepartment;

	/**
	 * 三级部门
	 */
	private String thirdDepartment;

	/**
	 * 应用描述
	 */
	private String description;

	/**
	 * 应用扩展信息
	 */
	private AppExtendJavaCreationParam extendParam;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getLanguageType() {
		return languageType;
	}

	public void setLanguageType(Integer languageType) {
		this.languageType = languageType;
	}

	public String getBaseImageVersion() {
		return baseImageVersion;
	}

	public void setBaseImageVersion(String baseImageVersion) {
		this.baseImageVersion = baseImageVersion;
	}

	public Integer getBaseImageSource() {
		return baseImageSource;
	}

	public void setBaseImageSource(Integer baseImageSource) {
		this.baseImageSource = baseImageSource;
	}

	public String getBaseImage() {
		return baseImage;
	}

	public void setBaseImage(String baseImage) {
		this.baseImage = baseImage;
	}

	public String getCodeRepoPath() {
		return codeRepoPath;
	}

	public void setCodeRepoPath(String codeRepoPath) {
		this.codeRepoPath = codeRepoPath;
	}

	public String getFirstDepartment() {
		return firstDepartment;
	}

	public void setFirstDepartment(String firstDepartment) {
		this.firstDepartment = firstDepartment;
	}

	public String getSecondDepartment() {
		return secondDepartment;
	}

	public void setSecondDepartment(String secondDepartment) {
		this.secondDepartment = secondDepartment;
	}

	public String getThirdDepartment() {
		return thirdDepartment;
	}

	public void setThirdDepartment(String thirdDepartment) {
		this.thirdDepartment = thirdDepartment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AppExtendJavaCreationParam getExtendParam() {
		return extendParam;
	}

	public void setExtendParam(AppExtendJavaCreationParam extendParam) {
		this.extendParam = extendParam;
	}

	/**
	 * 新增java扩展应用参数模型。
	 * 
	 * @author Dahai 2021-09-08
	 */
	public static class AppExtendJavaCreationParam implements Serializable {

		private static final long serialVersionUID = 1L;

		/**
		 * 应用编号
		 */
		private String appId;

		/**
		 * 构建方式，1：maven，2：gradle
		 */
		private Integer packageBuildType;

		/**
		 * 文件类型，1：jar，2：war，3：zip
		 */
		private Integer packageFileType;

		/**
		 * 打包文件路径
		 */
		private String packageTargetPath;

		public String getAppId() {
			return appId;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}

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
}