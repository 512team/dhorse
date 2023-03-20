package org.dhorse.application.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.MetricsTypeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.app.env.replica.DownloadFileParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaRebuildParam;
import org.dhorse.api.param.app.env.replica.QueryFilesParam;
import org.dhorse.api.param.app.env.replica.ReplicaMetricsQueryParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.vo.ClusterNamespace;
import org.dhorse.api.vo.EnvReplica;
import org.dhorse.api.vo.ReplicaMetricsAgg;
import org.dhorse.infrastructure.context.AppEnvClusterContext;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.AppParam;
import org.dhorse.infrastructure.param.ClusterParam;
import org.dhorse.infrastructure.param.ReplicaMetricsParam;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.BaseAppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.ReplicaMetricsPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.DateUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.custom.PodMetricsList;
import io.kubernetes.client.custom.Quantity;

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
	
	public void clearHistoryReplicaMetrics() {
		//删除3天前的数据
		replicaMetricsRepository.delete(DateUtils.addDays(new Date(), -3));
	}
	
	public void collectReplicaMetrics() {
		List<ClusterPO> clusters = clusterRepository.list(new ClusterParam());
		if(CollectionUtils.isEmpty(clusters)) {
			return;
		}
		List<AppEnvPO> envs = appEnvRepository.list(new AppEnvParam());
		if(CollectionUtils.isEmpty(envs)) {
			return;
		}
		List<String> appIds = envs.stream().map(e -> e.getAppId()).collect(Collectors.toList());
		AppParam appParam = new AppParam();
		appParam.setIds(appIds);
		List<AppPO> apps = appRepository.list(appParam);
		if(CollectionUtils.isEmpty(apps)) {
			return;
		}
		
		Map<String, AppPO> appMap = apps.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
		Map<String, AppEnvPO> envMap = new HashMap<>();
		for(AppEnvPO env : envs) {
			AppPO app = appMap.get(env.getAppId());
			if(app == null) {
				continue;
			}
			envMap.put(K8sUtils.getReplicaAppName(app.getAppName(), env.getTag()), env);
		}
		
		List<ReplicaMetricsParam> metricsList = new ArrayList<>();
		for(ClusterPO cluster : clusters){
			ClusterStrategy clusterStrategy = clusterStrategy(cluster.getClusterType());
			List<ClusterNamespace> namespaces = clusterStrategy.namespaceList(cluster, null);
			for(ClusterNamespace n : namespaces) {
				PodMetricsList podMetricsList = clusterStrategy.replicaMetrics(cluster, n.getNamespaceName());
				List<PodMetrics> metrics = podMetricsList.getItems();
				for(PodMetrics metric : metrics) {
					String replicaName = metric.getMetadata().getName();
					AppEnvPO appEnvPO = envMap.get(replicaName.substring(0, replicaName.indexOf("-dhorse-") + 7));
					if(appEnvPO == null) {
						continue;
					}
					if(CollectionUtils.isEmpty(metric.getContainers())) {
						continue;
					}
					Map<String, Quantity> usage = metric.getContainers().get(0).getUsage();
					ReplicaMetricsParam cpu = new ReplicaMetricsParam();
					cpu.setAppId(appEnvPO.getAppId());
					cpu.setReplicaName(replicaName);
					cpu.setMetricsType(MetricsTypeEnum.CPU.getCode());
					cpu.setUsedValue(usage.get("cpu").getNumber().movePointRight(3).setScale(0, RoundingMode.HALF_UP).longValue());
					cpu.setMinValue(Long.valueOf(appEnvPO.getReplicaCpu()));
					cpu.setMaxValue(Long.valueOf(appEnvPO.getReplicaCpu()));
					metricsList.add(cpu);
					
					ReplicaMetricsParam memory = new ReplicaMetricsParam();
					memory.setAppId(appEnvPO.getAppId());
					memory.setReplicaName(replicaName);
					memory.setMetricsType(MetricsTypeEnum.MEMORY.getCode());
					memory.setUsedValue(usage.get("memory").getNumber().divide(new BigDecimal(1024 * 1024)).setScale(0, RoundingMode.HALF_UP).longValue());
					memory.setMinValue(Long.valueOf(appEnvPO.getReplicaMemory()));
					memory.setMaxValue(Long.valueOf(appEnvPO.getReplicaMemory()));
					metricsList.add(memory);
				}
			}
		}
		replicaMetricsRepository.addList(metricsList);
	}
	
	public ReplicaMetricsAgg replicaMetricsAgg(LoginUser loginUser, ReplicaMetricsQueryParam queryParam) {
		this.hasRights(loginUser, queryParam.getAppId());
		
		ReplicaMetricsParam bizParam = new ReplicaMetricsParam();
		bizParam.setAppId(queryParam.getAppId());
		bizParam.setReplicaName(queryParam.getReplicaName());
		bizParam.setMetricsType(queryParam.getMetricsType());
		bizParam.setStartTime(queryParam.getStartTime());
		bizParam.setEndTime(queryParam.getEndTime());
		List<ReplicaMetricsPO> pos = replicaMetricsRepository.list(bizParam);
		ReplicaMetricsAgg agg = new ReplicaMetricsAgg();
		if(CollectionUtils.isEmpty(pos)) {
			return agg;
		}
		
		List<Long> maxValues = new ArrayList<>();
		List<Long> usedValues = new ArrayList<>();
		List<String> times = new ArrayList<>();
		for(int i = pos.size() - 1; i >= 0; i--) {
			ReplicaMetricsPO po = pos.get(i);
			maxValues.add(po.getMaxValue());
			usedValues.add(po.getUsedValue());
			times.add(DateUtils.formatDefault(po.getCreationTime()));
		}
		
		agg.setReplicaName(queryParam.getReplicaName());
		agg.setMetricsType(queryParam.getMetricsType());
		agg.setMaxValues(maxValues);
		agg.setUsedValues(usedValues);
		agg.setTimes(times);
		return agg;
	}
}