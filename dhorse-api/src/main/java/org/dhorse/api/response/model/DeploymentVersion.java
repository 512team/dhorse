package org.dhorse.api.response.model;

/**
 * 部署版本
 * 
 * @author Dahai 2021-09-08
 */
public class DeploymentVersion extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 分支名称
	 */
	private String branchName;

	/**
	 * 镜像名称（包含tag）
	 */
	private String versionName;

	/**
	 * 状态，0：构建中，1：构建成功，2：构建失败
	 */
	private Integer status;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}