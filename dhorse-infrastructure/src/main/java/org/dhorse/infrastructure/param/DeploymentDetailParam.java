package org.dhorse.infrastructure.param;

import java.util.Date;
import java.util.List;

/**
 * 分支部署明细参数
 * 
 * @author Dahai 2021-09-08
 */
public class DeploymentDetailParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 分支编号
	 */
	private String branchName;

	/**
	 * 镜像名称（包含tag）
	 */
	private String versionName;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 部署状态，0：部署待审批，1：部署中，2：部署成功，3：部署失败，4：合并成功，5：合并失败，6：回滚待审批，7：回滚中，8：回滚成功，9：回滚失败
	 */
	private Integer deploymentStatus;

	private List<Integer> deploymentStatuss;

	/**
	 * 部署线程
	 */
	private String deploymentThread;

	/**
	 * 部署人
	 */
	private String deployer;

	/**
	 * 审批者
	 */
	private String approver;

	/**
	 * 部署开始时间
	 */
	private Date startTime;

	/**
	 * 部署结束时间
	 */
	private Date endTime;

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

	public Integer getDeploymentStatus() {
		return deploymentStatus;
	}

	public void setDeploymentStatus(Integer deploymentStatus) {
		this.deploymentStatus = deploymentStatus;
	}

	public List<Integer> getDeploymentStatuss() {
		return deploymentStatuss;
	}

	public void setDeploymentStatuss(List<Integer> deploymentStatuss) {
		this.deploymentStatuss = deploymentStatuss;
	}

	public String getDeploymentThread() {
		return deploymentThread;
	}

	public void setDeploymentThread(String deploymentThread) {
		this.deploymentThread = deploymentThread;
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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}