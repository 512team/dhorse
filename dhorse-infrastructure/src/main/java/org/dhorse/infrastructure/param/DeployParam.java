package org.dhorse.infrastructure.param;

import java.io.Serializable;
import java.util.Date;

/**
 * 部署分支的参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class DeployParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 部署明细记录编号
	 */
	private String deploymentDetailId;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 部署版本
	 */
	private String versionName;

	/**
	 * 分支编号
	 */
	private String branchName;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 部署者
	 */
	private String deployer;

	/**
	 * 审核者
	 */
	private String approver;

	/**
	 * 部署开始时间
	 */
	private Date deploymentStartTime;

	public String getDeploymentDetailId() {
		return deploymentDetailId;
	}

	public void setDeploymentDetailId(String deploymentDetailId) {
		this.deploymentDetailId = deploymentDetailId;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public String getDeployer() {
		return deployer;
	}

	public void setDeployer(String deployer) {
		this.deployer = deployer;
	}

	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}

	public Date getDeploymentStartTime() {
		return deploymentStartTime;
	}

	public void setDeploymentStartTime(Date deploymentStartTime) {
		this.deploymentStartTime = deploymentStartTime;
	}

}