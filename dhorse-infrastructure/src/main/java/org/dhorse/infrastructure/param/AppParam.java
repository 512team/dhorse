package org.dhorse.infrastructure.param;

import java.util.List;

import org.dhorse.api.response.model.App.AppExtend;

/**
 * 应用信息参数
 * 
 * @author Dahai 2021-09-08
 */
public class AppParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 技术类型，1：SpringBoot，2：Node
	 */
	private Integer techType;

	/**
	 * 基础镜像版本
	 */
	private String baseImageVersion;

	/**
	 * 基础镜像来源，1：版本号，2：自定义
	 */
	private Integer baseImageSource;

	/**
	 * 基础镜像，如：openjdk:11.0.16-jdk
	 */
	private String baseImage;

	/**
	 * 代码仓库地址
	 */
	private String codeRepoPath;

	/**
	 * 亲密应用名称
	 */
	private List<String> affinityAppNames;

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
	 * 应用扩展信息，分页查询时不返回数据
	 */
	private AppExtend appExtend;

	public String getAppId() {
		return appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getTechType() {
		return techType;
	}

	public void setTechType(Integer techType) {
		this.techType = techType;
	}

	public Integer getBaseImageSource() {
		return baseImageSource;
	}

	public void setBaseImageSource(Integer baseImageSource) {
		this.baseImageSource = baseImageSource;
	}

	public String getBaseImageVersion() {
		return baseImageVersion;
	}

	public void setBaseImageVersion(String baseImageVersion) {
		this.baseImageVersion = baseImageVersion;
	}

	public String getBaseImage() {
		return baseImage;
	}

	public void setBaseImage(String baseImage) {
		this.baseImage = baseImage;
	}

	public List<String> getAffinityAppNames() {
		return affinityAppNames;
	}

	public void setAffinityAppNames(List<String> affinityAppNames) {
		this.affinityAppNames = affinityAppNames;
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

	public AppExtend getAppExtend() {
		return appExtend;
	}

	public void setAppExtend(AppExtend appExtend) {
		this.appExtend = appExtend;
	}

}