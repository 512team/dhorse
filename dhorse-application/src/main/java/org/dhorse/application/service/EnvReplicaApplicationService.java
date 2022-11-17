package org.dhorse.application.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.app.env.replica.DownloadFileParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaRebuildParam;
import org.dhorse.api.param.app.env.replica.QueryFilesParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.EnvReplica;
import org.dhorse.infrastructure.context.AppEnvClusterContext;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.repository.po.BaseAppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
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
public class EnvReplicaApplicationService extends BaseApplicationService<EnvReplica, BaseAppPO> {

	private static final Logger logger = LoggerFactory.getLogger(EnvReplicaApplicationService.class);

	public PageData<EnvReplica> page(LoginUser loginUser, EnvReplicaPageParam pageParam) {
		if(pageParam.getAppEnvId() == null) {
			return zeroPageData();
		}
		AppPO appPO = rightsApp(pageParam.getAppId(), loginUser);
		if(appPO == null) {
			return zeroPageData();
		}
		AppEnvParam appEnvParam = new AppEnvParam();
		appEnvParam.setAppId(pageParam.getAppId());
		appEnvParam.setId(pageParam.getAppEnvId());
		AppEnvPO appEnvPO = appEnvRepository.query(appEnvParam);
		if(appEnvPO == null) {
			return zeroPageData();
		}
		ClusterPO clusterPO = clusterRepository.queryById(appEnvPO.getClusterId());
		PageData<EnvReplica> pageData = clusterStrategy(clusterPO.getClusterType())
				.replicaPage(pageParam, clusterPO, appPO, appEnvPO);
		
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
		AppEnvClusterContext appEnvClusterEntity = queryCluster(param.getReplicaName(),
				loginUser);
		clusterStrategy(appEnvClusterEntity.getClusterPO().getClusterType()).rebuildReplica(
				appEnvClusterEntity.getClusterPO(), param.getReplicaName(),
				appEnvClusterEntity.getAppEnvPO().getNamespaceName());
		return null;
	}

	private AppPO rightsApp(String appId, LoginUser loginUser) {
		if(!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			AppMemberParam appMemberParam = new AppMemberParam();
			appMemberParam.setAppId(appId);
			appMemberParam.setUserId(loginUser.getId());
			AppMemberPO appMemberPO = appMemberRepository.query(appMemberParam);
			if (appMemberPO == null) {
				return null;
			}
		}
		return appRepository.queryById(appId);
	}

	public InputStream streamPodLog(LoginUser loginUser, String replicaName) {
		AppEnvClusterContext appEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.streamPodLog(appEnvClusterEntity.getClusterPO(),
				replicaName,
				appEnvClusterEntity.getAppEnvPO().getNamespaceName());
	}

	public AppEnvClusterContext queryCluster(String podName, LoginUser loginUser) {
		if (StringUtils.isBlank(podName)) {
			LogUtils.throwException(logger, MessageCodeEnum.REQUIRED_REPLICA_NAME);
		}
		String[] appNameAndEnvTag = K8sUtils.appNameAndEnvTag(podName);
		AppPO appPO = appRepository.queryByAppName(appNameAndEnvTag[0]);

		this.hasRights(loginUser, appPO.getId());
		
		AppEnvParam envInfoParam = new AppEnvParam();
		envInfoParam.setAppId(appPO.getId());
		envInfoParam.setTag(appNameAndEnvTag[1]);
		AppEnvPO appEnvPO = appEnvRepository.query(envInfoParam);
		if (!appEnvPO.getTag().equals(appNameAndEnvTag[1])) {
			LogUtils.throwException(logger, MessageCodeEnum.REPLICA_NAME_INVALIDE);
		}
		ClusterPO clusterPO = clusterRepository.queryById(appEnvPO.getClusterId());
		AppEnvClusterContext appEnvClusterEntity = new AppEnvClusterContext();
		appEnvClusterEntity.setAppPO(appPO);
		appEnvClusterEntity.setAppEnvPO(appEnvPO);
		appEnvClusterEntity.setClusterPO(clusterPO);
		return appEnvClusterEntity;
	}
	
	public List<String> queryFiles(LoginUser loginUser, QueryFilesParam requestParam) {
		String replicaName = requestParam.getReplicaName();
		AppEnvClusterContext appEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.queryFiles(appEnvClusterEntity.getClusterPO(),
				replicaName,
				appEnvClusterEntity.getAppEnvPO().getNamespaceName());
	}
	
	public InputStream downloadFile(LoginUser loginUser, DownloadFileParam requestParam) {
		String replicaName = requestParam.getReplicaName();
		AppEnvClusterContext appEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.downloadFile(appEnvClusterEntity.getClusterPO(),
				appEnvClusterEntity.getAppEnvPO().getNamespaceName(),
				replicaName,
				requestParam.getFileName());
	}
	
	public String downloadLog(LoginUser loginUser, EnvReplicaParam requestParam) {
		String replicaName = requestParam.getReplicaName();
		AppEnvClusterContext appEnvClusterEntity = queryCluster(replicaName, loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.podLog(appEnvClusterEntity.getClusterPO(),
				replicaName, appEnvClusterEntity.getAppEnvPO().getNamespaceName());
	}
}