package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.cluster.ClusterCreationParam;
import org.dhorse.api.param.cluster.ClusterPageParam;
import org.dhorse.api.param.cluster.ClusterQueryParam;
import org.dhorse.api.param.cluster.ClusterSearchParam;
import org.dhorse.api.param.cluster.ClusterUpdateParam;
import org.dhorse.api.param.cluster.LogSwitchParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.Cluster;
import org.dhorse.api.vo.LogCollectorStatus;
import org.dhorse.infrastructure.param.ClusterParam;
import org.dhorse.infrastructure.param.ProjectEnvParam;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
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
		clusterStrategy(clusterCreationParam.getClusterType())
			.addNamespace(clusterPO, K8sUtils.DHORSE_NAMESPACE);
		//2.再保存集群信息
		ClusterParam clusterParam = new ClusterParam();
		clusterParam.setClusterName(clusterCreationParam.getClusterName());
		if (clusterRepository.query(clusterParam) != null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_NAME_EXISTENCE);
		}
		String clusterId = clusterRepository.add(buildBizParam(clusterCreationParam));
		if (clusterId == null) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		
		//首先开启日志收集
		if(clusterCreationParam.getLogSwitch() == null) {
			closeLogSwitch(clusterId);
		}else if(YesOrNoEnum.YES.getCode().equals(clusterCreationParam.getLogSwitch())) {
			openLogSwitch(clusterId);
		}
		
		return null;
	}

	public Void update(ClusterUpdateParam clusterUpdateParam) {
		if (StringUtils.isBlank(clusterUpdateParam.getClusterId())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		if (clusterRepository.queryById(clusterUpdateParam.getClusterId()) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		validateAddParam(clusterUpdateParam);
		
		ClusterParam clusterParam = buildBizParam(clusterUpdateParam);
		clusterParam.setId(clusterUpdateParam.getClusterId());
		if (!clusterRepository.update(clusterParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		
		//开启（关闭）日志收集
		if(clusterUpdateParam.getLogSwitch() == null) {
			closeLogSwitch(clusterUpdateParam.getClusterId());
		}else if(YesOrNoEnum.YES.getCode().equals(clusterUpdateParam.getLogSwitch())) {
			openLogSwitch(clusterUpdateParam.getClusterId());
		}
				
		return null;
	}

	public Void delete(String clusterId) {
		if (StringUtils.isBlank(clusterId)) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		ProjectEnvParam projectEnvParam = new ProjectEnvParam();
		projectEnvParam.setClusterId(clusterId);
		List<ProjectEnvPO> projectEnvPOs = projectEnvRepository.list(projectEnvParam);
		if(!CollectionUtils.isEmpty(projectEnvPOs)) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_DELETED);
		}
		if (clusterRepository.queryById(clusterId) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		if (!clusterRepository.delete(clusterId)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}

	private void validateAddParam(ClusterCreationParam clusterCreationParam) {
		if (StringUtils.isBlank(clusterCreationParam.getClusterName())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_NAME_IS_EMPTY);
		}
		if (StringUtils.isBlank(clusterCreationParam.getClusterUrl())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_URL_IS_EMPTY);
		}
		if (StringUtils.isBlank(clusterCreationParam.getAuthName())
				&& StringUtils.isBlank(clusterCreationParam.getAuthToken())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSTER_AUTH_IS_EMPTY);
		}
		if (StringUtils.isNotBlank(clusterCreationParam.getAuthName())
				&& StringUtils.isBlank(clusterCreationParam.getAuthPassword())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSTER_AUTHP_ASSWORD_IS_EMPTY);
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
