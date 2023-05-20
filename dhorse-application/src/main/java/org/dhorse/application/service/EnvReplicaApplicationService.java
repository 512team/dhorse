package org.dhorse.application.service;

import java.io.InputStream;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.MetricsTypeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.TechTypeEnum;
import org.dhorse.api.param.app.env.replica.DownloadFileParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaRebuildParam;
import org.dhorse.api.param.app.env.replica.MetricsQueryParam;
import org.dhorse.api.param.app.env.replica.QueryFilesParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppEnv.EnvExtendSpringBoot;
import org.dhorse.api.response.model.ClusterNamespace;
import org.dhorse.api.response.model.EnvReplica;
import org.dhorse.api.response.model.Metrics;
import org.dhorse.api.response.model.MetricsView;
import org.dhorse.infrastructure.context.AppEnvClusterContext;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.AppParam;
import org.dhorse.infrastructure.param.ClusterParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.param.MetricsParam;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.BaseAppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.MetricsPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DateUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
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
			if(TechTypeEnum.SPRING_BOOT.getCode().equals(appPO.getTechType())
					&& !StringUtils.isBlank(appEnvPO.getExt())) {
				e.setJvmMetricsStatus(JsonUtils.parseToObject(appEnvPO.getExt(),
						EnvExtendSpringBoot.class).getJvmMetricsStatus());
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
	
	public void clearMetrics(Date date) {
		metricsRepository.delete(date);
	}
	
	public void collectReplicaMetrics() {
		//这里随机休眠一段时间，在集群部署时防止多个节点的任务并发执行
		try {
			Thread.sleep(new Random().nextInt(100));
		} catch (InterruptedException e) {
			//ignore
		}
		// 如果修改不成功，则代表其他节点已经运行了任务，该节点不需要再运行
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.COLLECT_REPLICA_METRICS_TASK_TIME.getCode());
		globalConfigParam.setItemValue(DateUtils.formatDefault(new Date()));
		if(!globalConfigRepository.updateByMoreCondition(globalConfigParam)) {
			return;
		}
		
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
		
		for(ClusterPO cluster : clusters){
			ClusterStrategy clusterStrategy = clusterStrategy(cluster.getClusterType());
			List<ClusterNamespace> namespaces = clusterStrategy.namespaceList(cluster, null);
			for(ClusterNamespace n : namespaces) {
				PodMetricsList podMetricsList = clusterStrategy.replicaMetrics(cluster, n.getNamespaceName());
				if(podMetricsList == null) {
					continue;
				}
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
					
					List<MetricsParam> metricsList = new ArrayList<>();
					Map<String, Quantity> usage = metric.getContainers().get(0).getUsage();
					long cpuUsed = usage.get("cpu").getNumber().movePointRight(3).setScale(0, RoundingMode.HALF_UP).longValue();
					item(MetricsTypeEnum.REPLICA_CPU_USED, replicaName, metricsList, cpuUsed);
					item(MetricsTypeEnum.REPLICA_CPU_MAX, replicaName, metricsList, appEnvPO.getReplicaCpu());
					
					long memoryUsed = usage.get("memory").getNumber().setScale(0, RoundingMode.HALF_UP).longValue();
					item(MetricsTypeEnum.REPLICA_MEMORY_USED, replicaName, metricsList, memoryUsed);
					item(MetricsTypeEnum.REPLICA_MEMORY_MAX, replicaName, metricsList, appEnvPO.getReplicaMemory() * Constants.ONE_MB);
					metricsRepository.addList(metricsList);
				}
			}
		}
	}
	
	public MetricsView metrics(LoginUser loginUser, MetricsQueryParam queryParam) {
		if(StringUtils.isBlank(queryParam.getStartTime())
				|| StringUtils.isBlank(queryParam.getEndTime())
				|| null == queryParam.getMetricsType()
				|| StringUtils.isBlank(queryParam.getReplicaName())){
			LogUtils.throwException(logger, MessageCodeEnum.INVALID_PARAM);
		}
		MetricsParam bizParam = new MetricsParam();
		bizParam.setReplicaName(queryParam.getReplicaName());
		bizParam.setMetricsType(queryParam.getMetricsType());
		bizParam.setStartTime(queryParam.getStartTime());
		bizParam.setEndTime(queryParam.getEndTime());
		List<MetricsPO> pos = metricsRepository.list(bizParam);
		MetricsView rm = new MetricsView();
		if(CollectionUtils.isEmpty(pos)) {
			return rm;
		}
		MetricsTypeEnum type = MetricsTypeEnum.getByCode(queryParam.getMetricsType());
		List<Long> metricsValues = new ArrayList<>();
		List<String> times = new ArrayList<>();
		for(int i = pos.size() - 1; i >= 0; i--) {
			MetricsPO po = pos.get(i);
			times.add(DateUtils.formatDefault(po.getCreationTime()));
			if(po.getMetricsValue() < 0) {
				continue;
			}
			if(Constants.MB_UNIT.equals(type.getUnit())) {
				metricsValues.add(po.getMetricsValue() / Constants.ONE_MB);
			}else {
				metricsValues.add(po.getMetricsValue());
			}
		}
		
		rm.setReplicaName(queryParam.getReplicaName());
		rm.setMetricsType(queryParam.getMetricsType());
		rm.setFirstTypeName(type.getFirstTypeName());
		rm.setSecondeTypeName(type.getSecondeTypeName());
		rm.setUnit(type.getUnit());
		rm.setMetricsValues(metricsValues);
		rm.setTimes(times);
		return rm;
	}
	
	private void item(MetricsTypeEnum metricsType, String replicaName,
			List<MetricsParam> metricsList, long metricsValue) {
		MetricsParam m = new MetricsParam();
		m.setReplicaName(replicaName);
		m.setMetricsType(metricsType.getCode());
		m.setMetricsValue(metricsValue);
		metricsList.add(m);
	}
	
	public Void metricsAdd(List<Metrics> param) {
		if(CollectionUtils.isEmpty(param)) {
			return null;
		}
		List<MetricsParam> metricsParam = param.stream().map(e -> BeanUtils
				.copyProperties(e, MetricsParam.class)).collect(Collectors.toList());
		metricsRepository.addList(metricsParam);
		return null;
	}
}