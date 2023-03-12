package org.dhorse.infrastructure.utils;

import java.util.Date;

import org.dhorse.api.vo.App;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.repo.CodeRepoStrategy;

/**
 * 
 * 部署分支参数数据模型
 * 
 * @author Dahai 2021-9-20 11:37:16
 */
public class DeployContext {

	// 提交人
	private String submitter;

	// 审批人
	private String approver;

	private GlobalConfigAgg globalConfigAgg;

	private ClusterPO cluster;

	private ComponentConstants componentConstants;

	private App app;

	private String branchName;

	private AppEnvPO appEnv;

	private CodeRepoStrategy codeRepoStrategy;

	private ClusterStrategy clusterStrategy;

	private String localPathOfBranch;

	private String versionName;

	private String fullNameOfImage;

	private String id;

	private Date startTime;

	private String deploymentName;

	/**
	 * 日志文件路径
	 */
	private String logFilePath;

	/**
	 * 代理镜像名称
	 */
	private String fullNameOfAgentImage;

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}

	public ClusterPO getCluster() {
		return cluster;
	}

	public void setCluster(ClusterPO cluster) {
		this.cluster = cluster;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public GlobalConfigAgg getGlobalConfigAgg() {
		return globalConfigAgg;
	}

	public void setGlobalConfigAgg(GlobalConfigAgg globalConfigAgg) {
		this.globalConfigAgg = globalConfigAgg;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getFullNameOfImage() {
		return fullNameOfImage;
	}

	public void setFullNameOfImage(String fullNameOfImage) {
		this.fullNameOfImage = fullNameOfImage;
	}

	public String getLocalPathOfBranch() {
		return localPathOfBranch;
	}

	public void setLocalPathOfBranch(String localPathOfBranch) {
		this.localPathOfBranch = localPathOfBranch;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	public AppEnvPO getAppEnv() {
		return appEnv;
	}

	public void setAppEnv(AppEnvPO appEnv) {
		this.appEnv = appEnv;
	}

	public ComponentConstants getComponentConstants() {
		return componentConstants;
	}

	public void setComponentConstants(ComponentConstants componentConstants) {
		this.componentConstants = componentConstants;
	}

	public CodeRepoStrategy getCodeRepoStrategy() {
		return codeRepoStrategy;
	}

	public void setCodeRepoStrategy(CodeRepoStrategy codeRepoStrategy) {
		this.codeRepoStrategy = codeRepoStrategy;
	}

	public ClusterStrategy getClusterStrategy() {
		return clusterStrategy;
	}

	public void setClusterStrategy(ClusterStrategy clusterStrategy) {
		this.clusterStrategy = clusterStrategy;
	}

	public String getLogFilePath() {
		return logFilePath;
	}

	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	public String getFullNameOfAgentImage() {
		return fullNameOfAgentImage;
	}

	public void setFullNameOfAgentImage(String fullNameOfAgentImage) {
		this.fullNameOfAgentImage = fullNameOfAgentImage;
	}

}
