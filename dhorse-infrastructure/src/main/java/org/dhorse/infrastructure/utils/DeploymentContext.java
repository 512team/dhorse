package org.dhorse.infrastructure.utils;

import java.util.Date;
import java.util.List;

import org.dhorse.api.enums.EventTypeEnum;
import org.dhorse.api.response.model.App;
import org.dhorse.api.response.model.AppEnv.EnvExtend;
import org.dhorse.api.response.model.EnvHealth;
import org.dhorse.api.response.model.EnvLifecycle;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.repository.po.AffinityTolerationPO;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.repo.CodeRepoStrategy;

/**
 * 
 * 部署分支模型
 * 
 * @author Dahai 2021-9-20 11:37:16
 */
public class DeploymentContext {

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

	private EnvExtend envExtend;

	private EnvHealth envHealth;

	private EnvLifecycle envLifecycle;

	private List<AffinityTolerationPO> affinitys;

	private CodeRepoStrategy codeRepoStrategy;

	private ClusterStrategy clusterStrategy;

	private String localPathOfBranch;

	private String versionName;

	private String fullNameOfImage;

	private String id;

	private Date startTime;

	private String deploymentName;

	/**
	 * 事件类型
	 */
	private EventTypeEnum eventType;

	/**
	 * 日志文件路径
	 */
	private String logFilePath;

	/**
	 * 链路追踪镜像名称
	 */
	private String fullNameOfTraceAgentImage;

	/**
	 * DHorse代理镜像名称
	 */
	private String fullNameOfDHorseAgentImage;

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

	@SuppressWarnings("unchecked")
	public <T extends EnvExtend> T getEnvExtend() {
		return (T) envExtend;
	}

	public void setEnvExtend(EnvExtend envExtend) {
		this.envExtend = envExtend;
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

	public EnvHealth getEnvHealth() {
		return envHealth;
	}

	public void setEnvHealth(EnvHealth envHealth) {
		this.envHealth = envHealth;
	}

	public EnvLifecycle getEnvLifecycle() {
		return envLifecycle;
	}

	public void setEnvLifecycle(EnvLifecycle envLifecycle) {
		this.envLifecycle = envLifecycle;
	}

	public List<AffinityTolerationPO> getAffinitys() {
		return affinitys;
	}

	public void setAffinitys(List<AffinityTolerationPO> affinitys) {
		this.affinitys = affinitys;
	}

	public EventTypeEnum getEventType() {
		return eventType;
	}

	public void setEventType(EventTypeEnum eventType) {
		this.eventType = eventType;
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

	public String getFullNameOfTraceAgentImage() {
		return fullNameOfTraceAgentImage;
	}

	public void setFullNameOfTraceAgentImage(String fullNameOfTraceAgentImage) {
		this.fullNameOfTraceAgentImage = fullNameOfTraceAgentImage;
	}

	public String getFullNameOfDHorseAgentImage() {
		return fullNameOfDHorseAgentImage;
	}

	public void setFullNameOfDHorseAgentImage(String fullNameOfDHorseAgentImage) {
		this.fullNameOfDHorseAgentImage = fullNameOfDHorseAgentImage;
	}

}
