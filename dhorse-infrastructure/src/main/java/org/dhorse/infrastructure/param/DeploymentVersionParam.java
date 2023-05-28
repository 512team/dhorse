package org.dhorse.infrastructure.param;

import java.util.Date;
import java.util.List;

/**
 * 部署版本
 * 
 * @author Dahai 2021-09-08
 */
public class DeploymentVersionParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 分支编号
	 */
	private String branchName;

	/**
	 * 版本名称
	 */
	private String versionName;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 环境编号
	 */
	private List<String> envIds;

	/**
	 * 状态，0：构建中，1：构建成功，2：构建失败
	 */
	private Integer status;

	private Date creationTime;

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

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public List<String> getEnvIds() {
		return envIds;
	}

	public void setEnvIds(List<String> envIds) {
		this.envIds = envIds;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

}