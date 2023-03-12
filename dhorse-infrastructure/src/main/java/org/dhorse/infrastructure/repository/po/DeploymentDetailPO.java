package org.dhorse.infrastructure.repository.po;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 分支部署明细表
 * 
 * @author Dahai 2021-09-08
 */
@TableName("DEPLOYMENT_DETAIL")
public class DeploymentDetailPO extends BaseAppPO {

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
	 * 部署状态，1：部署中，2：部署成功，3：部署失败，4：合并成功，5：合并失败，6：提交回滚，7：回滚中，8：回滚成功，9：回滚失败
	 */
	private Integer deploymentStatus;

	@TableField(exist = false)
	private List<Integer> deploymentStatuss;

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