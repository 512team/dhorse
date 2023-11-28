package org.dhorse.application.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.model.ReplicaMetrics;
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
import org.dhorse.infrastructure.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
		AppEnvClusterContext appEnvClusterEntity = queryCluster(param.getAppId(),
				param.getEnvId(), loginUser);
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

	public AppEnvClusterContext queryCluster(String appId, String envId, LoginUser loginUser) {
		if (StringUtils.isBlank(appId) || StringUtils.isBlank(envId)) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "应用编号或环境编号不能为空");
		}
		AppPO appPO = appRepository.queryById(appId);
		this.hasRights(loginUser, appPO.getId());
		AppEnvPO appEnvPO = appEnvRepository.queryById(envId);
		ClusterPO clusterPO = clusterRepository.queryById(appEnvPO.getClusterId());
		AppEnvClusterContext context = new AppEnvClusterContext();
		context.setAppPO(appPO);
		context.setAppEnvPO(appEnvPO);
		context.setClusterPO(clusterPO);
		return context;
	}
	
	public List<String> queryFiles(LoginUser loginUser, QueryFilesParam requestParam) {
		AppEnvClusterContext appEnvClusterEntity = queryCluster(requestParam.getAppId(),
				requestParam.getEnvId(), loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.queryFiles(appEnvClusterEntity.getClusterPO(),
				requestParam.getReplicaName(),
				appEnvClusterEntity.getAppEnvPO().getNamespaceName());
	}
	
	public InputStream downloadFile(LoginUser loginUser, DownloadFileParam requestParam) {
		AppEnvClusterContext appEnvClusterEntity = queryCluster(requestParam.getAppId(),
				requestParam.getEnvId(), loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.downloadFile(appEnvClusterEntity.getClusterPO(),
				appEnvClusterEntity.getAppEnvPO().getNamespaceName(),
				requestParam.getReplicaName(),
				requestParam.getFileName());
	}
	
	public String downloadLog(LoginUser loginUser, EnvReplicaParam requestParam) {
		AppEnvClusterContext appEnvClusterEntity = queryCluster(requestParam.getAppId(),
				requestParam.getEnvId(), loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.podLog(appEnvClusterEntity.getClusterPO(),
				requestParam.getReplicaName(),
				appEnvClusterEntity.getAppEnvPO().getNamespaceName());
	}

	public String downloadYaml(LoginUser loginUser, EnvReplicaParam requestParam) {
		AppEnvClusterContext appEnvClusterEntity = queryCluster(requestParam.getAppId(),
				requestParam.getEnvId(), loginUser);
		ClusterStrategy clusterStrategy = clusterStrategy(
				appEnvClusterEntity.getClusterPO().getClusterType());
		return clusterStrategy.podYaml(appEnvClusterEntity.getClusterPO(),
				requestParam.getReplicaName(),
				appEnvClusterEntity.getAppEnvPO().getNamespaceName());
	}

	public void clearMetrics(Date date) {
		metricsRepository.delete(date);
	}
	
	public void collectReplicaMetrics() {
		
		//随机休眠，减小集群任务的并发性
		randomSleep();
		
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
				List<ReplicaMetrics> replicaMetrics = clusterStrategy.replicaMetrics(cluster, n.getNamespaceName());
				if(CollectionUtils.isEmpty(replicaMetrics)) {
					continue;
				}
				for(ReplicaMetrics metric : replicaMetrics) {
					AppEnvPO appEnvPO = envMap.get(metric.getAppLabel());
					if(appEnvPO == null) {
						continue;
					}
					String replicaName = metric.getReplicaName();
					List<MetricsParam> metricsList = new ArrayList<>();
					item(MetricsTypeEnum.REPLICA_CPU_USED, replicaName, metricsList, metric.getCpuUsed());
					item(MetricsTypeEnum.REPLICA_CPU_MAX, replicaName, metricsList, appEnvPO.getReplicaCpu());
					item(MetricsTypeEnum.REPLICA_MEMORY_USED, replicaName, metricsList, metric.getMemoryUsed());
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