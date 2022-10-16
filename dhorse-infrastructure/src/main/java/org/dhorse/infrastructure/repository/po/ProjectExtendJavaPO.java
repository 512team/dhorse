package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("PROJECT_EXTEND_JAVA")
public class ProjectExtendJavaPO extends ProjectExtendPO {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

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

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
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

	@Override
	public List<String> getIds() {
		return this.ids;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Integer getDeletionStatus() {
		return this.deletionStatus;
	}

}