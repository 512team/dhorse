package org.dhorse.api.vo;

/**
 * 项目信息
 * 
 * @author Dahai 2021-09-08
 */
public class Project extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目名称
	 */
	private String projectName;

	/**
	 * 开发语言类型，1：Java，2：Python，3：Node，4：App，5：H5，6：C，7：C++，8：Go
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

	/**
	 * 项目扩展信息，分页查询时不返回数据
	 */
	private ProjectExtend projectExtend;

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

	@SuppressWarnings("unchecked")
	public <T extends ProjectExtend> T getProjectExtend() {
		return (T) projectExtend;
	}

	public void setProjectExtend(ProjectExtend projectExtend) {
		this.projectExtend = projectExtend;
	}

	public static abstract class ProjectExtend extends BaseDto {

		private static final long serialVersionUID = 1L;

		/**
		 * 项目编号
		 */
		private String projectId;

		public String getProjectId() {
			return projectId;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}
	}
}