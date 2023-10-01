package org.dhorse.api.response.model;

import org.dhorse.api.response.model.App.AppExtend;

public class AppExtendNodejs extends AppExtend {

	private static final long serialVersionUID = 1L;

	/**
	 * node版本
	 */
	private String nodeVersion;

	/**
	 * Node镜像
	 */
	private String nodeImage;

	/**
	 * npm版本
	 */
	private String npmVersion;

	/**
	 * pnpm版本
	 */
	private String pnpmVersion;

	/**
	 * yarn版本
	 */
	private String yarnVersion;

	/**
	 * 类型
	 */
	private Integer compileType;

	/**
	 * 启动文件
	 */
	private String startFile;

	public String getNodeVersion() {
		return nodeVersion;
	}

	public void setNodeVersion(String nodeVersion) {
		this.nodeVersion = nodeVersion;
	}

	public String getNodeImage() {
		return nodeImage;
	}

	public void setNodeImage(String nodeImage) {
		this.nodeImage = nodeImage;
	}

	public String getStartFile() {
		return startFile;
	}

	public void setStartFile(String startFile) {
		this.startFile = startFile;
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

	public String getYarnVersion() {
		return yarnVersion;
	}

	public void setYarnVersion(String yarnVersion) {
		this.yarnVersion = yarnVersion;
	}

	public Integer getCompileType() {
		return compileType;
	}

	public void setCompileType(Integer compileType) {
		this.compileType = compileType;
	}

}