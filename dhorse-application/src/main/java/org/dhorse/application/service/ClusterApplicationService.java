package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.AuthTypeEnum;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.cluster.ClusterCreationParam;
import org.dhorse.api.param.cluster.ClusterPageParam;
import org.dhorse.api.param.cluster.ClusterQueryParam;
import org.dhorse.api.param.cluster.ClusterSearchParam;
import org.dhorse.api.param.cluster.ClusterUpdateParam;
import org.dhorse.api.param.cluster.LogSwitchParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.Cluster;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.LogCollectorStatus;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.ClusterParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 
 * 集群应用服务
 * 
 * @author 天地之怪
 */
@Service
public class ClusterApplicationService extends BaseApplicationService<Cluster, ClusterPO> {

	private static final Logger logger = LoggerFactory.getLogger(SysUserApplicationService.class);

	public PageData<Cluster> page(ClusterPageParam clusterPageParam) {
		return this.pageData(clusterRepository.page(buildBizParam(clusterPageParam)));
	}
	
	public Cluster query(ClusterQueryParam clusterQueryParam) {
		return this.po2Dto(clusterRepository.queryById(clusterQueryParam.getClusterId()));
	}
	
	public List<Cluster> search(ClusterSearchParam clusterSearchParam) {
		ClusterParam clusterParam = buildBizParam(clusterSearchParam);
		List<ClusterPO> pos = clusterRepository.list(clusterParam);
		if(CollectionUtils.isEmpty(pos)) {
			return Collections.emptyList();
		}
		return pos.stream().map(e ->{
			Cluster cluster = new Cluster();
			cluster.setId(e.getId());
			cluster.setClusterName(e.getClusterName());
			return cluster;
		}).collect(Collectors.toList());
	}

	public Void add(ClusterCreationParam clusterCreationParam) {
		validateAddParam(clusterCreationParam);
		ClusterPO clusterPO = new ClusterPO();
		clusterPO.setClusterUrl(clusterCreationParam.getClusterUrl());
		clusterPO.setAuthToken(clusterCreationParam.getAuthToken());
		//1.首先添加命名空间
		ClusterStrategy cluster = clusterStrategy(clusterCreationParam.getClusterType());
		cluster.addNamespace(clusterPO, K8sUtils.DHORSE_NAMESPACE);
		//2.往集群推送dhorse的服务地址
		cluster.createDHorseConfig(clusterPO);
		//3.创建镜像仓库认证key
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(globalConfigParam);
		if(globalConfigAgg != null && globalConfigAgg.getImageRepo() != null) {
			cluster.createSecret(clusterPO, globalConfigAgg.getImageRepo());
		}
		//4.保存集群信息
		ClusterParam clusterParam = new ClusterParam();
		clusterParam.setClusterName(clusterCreationParam.getClusterName());
		if (clusterRepository.query(clusterParam) != null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_NAME_EXISTENCE);
		}
		String clusterId = clusterRepository.add(buildBizParam(clusterCreationParam));
		if (clusterId == null) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		
		//5.开启（关闭）日志收集，不能在步骤4前执行
		if(clusterCreationParam.getLogSwitch() == null) {
			closeLogSwitch(clusterId);
		}else if(YesOrNoEnum.YES.getCode().equals(clusterCreationParam.getLogSwitch())) {
			openLogSwitch(clusterId);
		}
		
		return null;
	}

	public Void update(ClusterUpdateParam updateParam) {
		if (StringUtils.isBlank(updateParam.getClusterId())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		if (clusterRepository.queryById(updateParam.getClusterId()) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		validateAddParam(updateParam);
		
		//1.首先添加命名空间
		ClusterPO clusterPO = new ClusterPO();
		clusterPO.setClusterUrl(updateParam.getClusterUrl());
		clusterPO.setAuthToken(updateParam.getAuthToken());
		ClusterStrategy cluster = clusterStrategy(updateParam.getClusterType());
		cluster.addNamespace(clusterPO, K8sUtils.DHORSE_NAMESPACE);
		//2.往集群推送dhorse的服务地址
		cluster.createDHorseConfig(clusterPO);
		//3.创建镜像仓库认证key
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(globalConfigParam);
		if(globalConfigAgg != null && globalConfigAgg.getImageRepo() != null) {
			cluster.createSecret(clusterPO, globalConfigAgg.getImageRepo());
		}
		
		//4.修改集群
		ClusterParam clusterParam = buildBizParam(updateParam);
		clusterParam.setId(updateParam.getClusterId());
		if (!clusterRepository.update(clusterParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		
		//5.开启（关闭）日志收集，不能在步骤4前执行
		if(updateParam.getLogSwitch() == null) {
			closeLogSwitch(updateParam.getClusterId());
		}else if(YesOrNoEnum.YES.getCode().equals(updateParam.getLogSwitch())) {
			openLogSwitch(updateParam.getClusterId());
		}
		
		return null;
	}

	public Void delete(String clusterId) {
		if (StringUtils.isBlank(clusterId)) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		AppEnvParam appEnvParam = new AppEnvParam();
		appEnvParam.setClusterId(clusterId);
		List<AppEnvPO> appEnvPOs = appEnvRepository.list(appEnvParam);
		if(!CollectionUtils.isEmpty(appEnvPOs)) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ENV_DELETED);
		}
		if (clusterRepository.queryById(clusterId) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		if (!clusterRepository.delete(clusterId)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}

	private void validateAddParam(ClusterCreationParam addParam) {
		if(addParam.getAuthType() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.AUTH_TYPE_IS_EMPTY);
		}
		if (StringUtils.isBlank(addParam.getClusterName())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_NAME_IS_EMPTY);
		}
		if (StringUtils.isBlank(addParam.getClusterUrl())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_URL_IS_EMPTY);
		}
		if (AuthTypeEnum.TOKEN.getCode().equals(addParam.getAuthType())) {
			if(StringUtils.isBlank(addParam.getAuthToken())) {
				LogUtils.throwException(logger, MessageCodeEnum.AUTH_TOKEN_IS_EMPTY);
			}
			addParam.setAuthToken(addParam.getAuthToken().trim());
		}
		if (AuthTypeEnum.ACCOUNT.getCode().equals(addParam.getAuthType())
				&& (StringUtils.isNotBlank(addParam.getAuthName())
				|| StringUtils.isBlank(addParam.getAuthPassword()))) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSTER_AUTHP_ASSWORD_IS_EMPTY);
		}
		if(addParam.getClusterName().length() > 16) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "集群名称不能大于16个字符");
		}
		if(!addParam.getClusterUrl().startsWith("http")) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "集群地址格式不正确");
		}
		if(addParam.getClusterUrl().length() > 128) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "集群地址不能大于128个字符");
		}
		if(addParam.getDescription() != null && addParam.getDescription().length() > 128) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "集群描述不能大于128个字符");
		}
	}

	private ClusterParam buildBizParam(Serializable requestParam) {
		ClusterParam bizParam = new ClusterParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}

	public Void openLogSwitch(String clusterId) {
		if (StringUtils.isBlank(clusterId)) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		ClusterPO clusterPO = clusterRepository.queryById(clusterId);
		if (clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		clusterStrategy(clusterPO.getClusterType()).openLogCollector(clusterPO);
		return null;
	}

	public Void closeLogSwitch(String clusterId) {
		if (StringUtils.isBlank(clusterId)) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		ClusterPO clusterPO = clusterRepository.queryById(clusterId);
		if (clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		clusterStrategy(clusterPO.getClusterType()).closeLogCollector(clusterPO);
		return null;
	}

	public LogCollectorStatus logSwitchStatus(LogSwitchParam requestParam) {
		if (StringUtils.isBlank(requestParam.getClusterId())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		ClusterPO clusterPO = clusterRepository.queryById(requestParam.getClusterId());
		if (clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		LogCollectorStatus logCollectorStatus = new LogCollectorStatus();
		logCollectorStatus.setStatus(logSwitch(clusterPO));
		return logCollectorStatus;
	}
	
	private Integer logSwitch(ClusterPO clusterPO) {
		boolean status = clusterStrategy(clusterPO.getClusterType()).logSwitchStatus(clusterPO);
		LogCollectorStatus logCollectorStatus = new LogCollectorStatus();
		logCollectorStatus.setStatus(status ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
		return status ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode();
	}
	
	protected Cluster po2Dto(ClusterPO po) {
		Cluster dto = super.po2Dto(po);
		if(dto == null) {
			return null;
		}
		try {
			dto.setLogSwitch(logSwitch(po));
		}catch(Exception e) {
			dto.setLogSwitch(2);
		}
		return dto;
	}
}
