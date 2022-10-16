package org.dhorse.infrastructure.utils;

import java.util.Date;

import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.Project;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.repo.CodeRepoStrategy;

/**
 * 
 * 部署分支参数数据模型
 * 
 * @author Dahai 2021-9-20 11:37:16
 */
public class DeployContext {

	private GlobalConfigAgg globalConfigAgg;

	private ClusterPO cluster;

	private ComponentConstants componentConstants;

	private Project project;

	private String branchName;

	private ProjectEnvPO projectEnv;

	private CodeRepoStrategy codeRepoStrategy;

	private ClusterStrategy clusterStrategy;

	private String localPathOfBranch;

	private String nameOfImage;

	private String fullNameOfImage;

	private String id;

	private Date startTime;

	private String deploymentAppName;

	/**
	 * 日志文件路径
	 */
	private String logFilePath;

	public ClusterPO getCluster() {
		return cluster;
	}

	public void setCluster(ClusterPO cluster) {
		this.cluster = cluster;
	}

	public String getDeploymentAppName() {
		return deploymentAppName;
	}

	public void setDeploymentAppName(String deploymentAppName) {
		this.deploymentAppName = deploymentAppName;
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

	public String getNameOfImage() {
		return nameOfImage;
	}

	public void setNameOfImage(String nameOfImage) {
		this.nameOfImage = nameOfImage;
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

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ProjectEnvPO getProjectEnv() {
		return projectEnv;
	}

	public void setProjectEnv(ProjectEnvPO projectEnv) {
		this.projectEnv = projectEnv;
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

}
