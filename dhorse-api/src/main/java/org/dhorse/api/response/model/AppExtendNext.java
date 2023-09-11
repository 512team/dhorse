package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendNext extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * Node版本
	 */
	private String nodeVersion;

	/**
	 * Npm版本
	 */
	private String npmVersion;

	/**
	 * Pnpm版本
	 */
	private String pnpmVersion;

	/**
	 * Yarn版本
	 */
	private String yarnVersion;

	/**
	 * 编译方式
	 */
	private Integer compileType;

	/**
	 * 部署方式，1：动态部署，2：静态部署
	 */
	private Integer deploymentType;

	public String getNodeVersion() {
		return nodeVersion;
	}

	public void setNodeVersion(String nodeVersion) {
		this.nodeVersion = nodeVersion;
	}

	public String getNpmVersion() {
		return npmVersion;
	}

	public void setNpmVersion(String npmVersion) {
		this.npmVersion = npmVersion;
	}

	public String getPnpmVersion() {
		return pnpmVersion;
	}

	public void setPnpmVersion(String pnpmVersion) {
		this.pnpmVersion = pnpmVersion;
	}

	public Integer getCompileType() {
		return compileType;
	}

	public void setCompileType(Integer compileType) {
		this.compileType = compileType;
	}

	public String getYarnVersion() {
		return yarnVersion;
	}

	public void setYarnVersion(String yarnVersion) {
		this.yarnVersion = yarnVersion;
	}

	public Integer getDeploymentType() {
		return deploymentType;
	}

	public void setDeploymentType(Integer deploymentType) {
		this.deploymentType = deploymentType;
	}

}