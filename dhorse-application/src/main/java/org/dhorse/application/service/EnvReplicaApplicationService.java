package org.dhorse.application.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.project.env.replica.DownloadFileParam;
import org.dhorse.api.param.project.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.project.env.replica.EnvReplicaParam;
import org.dhorse.api.param.project.env.replica.EnvReplicaRebuildParam;
import org.dhorse.api.param.project.env.replica.QueryFilesParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.EnvReplica;
import org.dhorse.infrastructure.context.ProjectEnvClusterContext;
import org.dhorse.infrastructure.param.ProjectEnvParam;
import org.dhorse.infrastructure.param.ProjectMemberParam;
import org.dhorse.infrastructure.repository.po.BaseProjectPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 环境副本应用服务
 * 
 * @author 天地之怪
 */
@Service
public class EnvReplicaApplicationService extends BaseApplicationService<EnvReplica, BaseProjectPO> {

	private static final Logger logger = LoggerFactory.getLogger(EnvReplicaApplicationService.class);

	public PageData<EnvReplica> page(LoginUser loginUser, EnvReplicaPageParam pageParam) {
		if(pageParam.getProjectEnvId() == null) {
			return zeroPageData();
		}
		ProjectPO projectPO = rightsProject(pageParam.getProjectId(), loginUser);
		if(projectPO == null) {
			return zeroPageData();
		}
		ProjectEnvParam projectEnvParam = new ProjectEnvParam();
		projectEnvParam.setProjectId(pageParam.getProjectId());
		projectEnvParam.setId(pageParam.getProjectEnvId());
		ProjectEnvPO projectEnvPO = projectEnvRepository.query(projectEnvParam);
		if(projectEnvPO == null) {
			return zeroPageData();
		}
		ClusterPO clusterPO = clusterRepository.queryById(projectEnvPO.getClusterId());
		PageData<EnvReplica> pageData = clusterStrategy(clusterPO.getClusterType())
				.replicaPage(pageParam, clusterPO, projectPO, projectEnvPO);
		
		if(pageData.getItemCount() == 0) {
			return pageData;
		}
		
		Map<String, DeploymentVersionPO> versionCache = new HashMap<>();
		pageData.getItems().forEach(e -> {
			DeploymentVersionPO deploymentVersionPO = versionCache.get(e.getVersionName());
			if(deploymentVersionPO == null) {
				deploymentVersionPO = deploymentVersionRepository.queryByVersionName(e.getVersionName());
			}
			e.setBranchName(deploymentVersionPO != null ? deploymentVersionPO.getBranchName() : null);
		});
		
		return pageData;
	}

	public Void rebuild(LoginUser loginUser, EnvReplicaRebuildParam param) {
		ProjectEnvClusterContext projectEnvClusterEntity = queryCluster(param.getReplicaName(),
				loginUser);
		clusterStrategy(projectEnvClusterEntity.getClusterPO().getClusterType()).rebuildReplica(
				projectEnvClusterEntity.getClusterPO(), param.getReplicaName(),
				projectEnvClusterEntity.getProjectEnvPO().getNamespaceName());
		return null;
	}

	private ProjectPO rightsProject(String projectId, LoginUser loginUser) {
		if(!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			ProjectMemberParam projectMemberParam = new ProjectMemberParam();
			projectMemberParam.setProjectId(projectId);
			projectMemberParam.setUserId(loginUser.getId());
			ProjectMemberPO projectMemberPO = projectMemberRepository.query(projectMemberParam);
			if (projectMemberPO == null) {
				return null;
			}
		}
		return projectRepository.queryById(projectId);
	}

	public InputStream streamPodLog(LoginUser loginUser, String replicaName) {
		ProjectEnvClusterContext projectEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				projectEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.streamPodLog(projectEnvClusterEntity.getClusterPO(),
				replicaName,
				projectEnvClusterEntity.getProjectEnvPO().getNamespaceName());
	}

	public ProjectEnvClusterContext queryCluster(String podName, LoginUser loginUser) {
		if (StringUtils.isBlank(podName)) {
			LogUtils.throwException(logger, MessageCodeEnum.REQUIRED_REPLICA_NAME);
		}
		String[] projectNameAndEnvTag = K8sUtils.projectNameAndEnvTag(podName);
		ProjectPO projectPO = projectRepository.queryByProjectName(projectNameAndEnvTag[0]);

		this.hasRights(loginUser, projectPO.getId());
		
		ProjectEnvParam envInfoParam = new ProjectEnvParam();
		envInfoParam.setProjectId(projectPO.getId());
		envInfoParam.setTag(projectNameAndEnvTag[1]);
		ProjectEnvPO projectEnvPO = projectEnvRepository.query(envInfoParam);
		if (!projectEnvPO.getTag().equals(projectNameAndEnvTag[1])) {
			LogUtils.throwException(logger, MessageCodeEnum.REPLICA_NAME_INVALIDE);
		}
		ClusterPO clusterPO = clusterRepository.queryById(projectEnvPO.getClusterId());
		ProjectEnvClusterContext projectEnvClusterEntity = new ProjectEnvClusterContext();
		projectEnvClusterEntity.setProjectPO(projectPO);
		projectEnvClusterEntity.setProjectEnvPO(projectEnvPO);
		projectEnvClusterEntity.setClusterPO(clusterPO);
		return projectEnvClusterEntity;
	}
	
	public List<String> queryFiles(LoginUser loginUser, QueryFilesParam requestParam) {
		String replicaName = requestParam.getReplicaName();
		ProjectEnvClusterContext projectEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				projectEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.queryFiles(projectEnvClusterEntity.getClusterPO(),
				replicaName,
				projectEnvClusterEntity.getProjectEnvPO().getNamespaceName());
	}
	
	public InputStream downloadFile(LoginUser loginUser, DownloadFileParam requestParam) {
		String replicaName = requestParam.getReplicaName();
		ProjectEnvClusterContext projectEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				projectEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.downloadFile(projectEnvClusterEntity.getClusterPO(),
				projectEnvClusterEntity.getProjectEnvPO().getNamespaceName(),
				replicaName,
				requestParam.getFileName());
	}
	
	public String downloadLog(LoginUser loginUser, EnvReplicaParam requestParam) {
		String replicaName = requestParam.getReplicaName();
		ProjectEnvClusterContext projectEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				projectEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.podLog(projectEnvClusterEntity.getClusterPO(),
				replicaName, projectEnvClusterEntity.getProjectEnvPO().getNamespaceName());
	}
}