package org.dhorse.infrastructure.param;

/**
 * 项目信息参数
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目名称
	 */
	private String projectName;

	/**
	 * 开发语言类型，1：java，2：python，3：node，4：app，5：h5，6：c，7：c，8：go
	 */
	private Integer languageType;

	/**
	 * 依赖镜像，如：openjdk:8-jdk-alpine
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
	 * 项目描述
	 */
	private String description;

	public String getProjectId() {
		return projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getLanguageType() {
		return languageType;
	}

	public void setLanguageType(Integer languageType) {
		this.languageType = languageType;
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

}