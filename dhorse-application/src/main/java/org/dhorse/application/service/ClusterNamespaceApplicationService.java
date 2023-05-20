package org.dhorse.application.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.param.cluster.namespace.ClusterNamespaceCreationParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespaceDeletionParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespacePageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.ClusterNamespace;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.BasePO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 集群命名空间应用服务
 * 
 * @author 天地之怪
 */
@Service
public class ClusterNamespaceApplicationService extends BaseApplicationService<ClusterNamespace, BasePO> {

	private static final Logger logger = LoggerFactory.getLogger(ClusterNamespaceApplicationService.class);
	
	public PageData<ClusterNamespace> page(ClusterNamespacePageParam clusterNamespacePageParam) {
		if(StringUtils.isBlank(clusterNamespacePageParam.getClusterId())) {
			return zeroPageData();
		}
		ClusterPO clusterPO = clusterRepository.queryById(clusterNamespacePageParam.getClusterId());
		if (clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_EXISTENCE);
		}
		List<ClusterNamespace> namespaces = clusterStrategy(clusterPO.getClusterType())
				.namespaceList(clusterPO, clusterNamespacePageParam);
		PageData<ClusterNamespace> pageData = new PageData<>();
		pageData.setPageNum(clusterNamespacePageParam.getPageNum());
		pageData.setPageCount(1);
		pageData.setPageSize(clusterNamespacePageParam.getPageSize());
		pageData.setItemCount(namespaces.size());
		pageData.setItems(namespaces);
		return pageData;
	}

	public Void add(ClusterNamespaceCreationParam clusterNamespaceCreationParam) {
		validateAddParam(clusterNamespaceCreationParam);
		ClusterPO clusterPO = clusterRepository.queryById(clusterNamespaceCreationParam.getClusterId());
		if (clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_EXISTENCE);
		}
		
		ClusterStrategy cluster = clusterStrategy(clusterPO.getClusterType());
		
		//1.添加命名空间
		cluster.addNamespace(clusterPO,
				clusterNamespaceCreationParam.getNamespaceName());
		//2.往集群推送dhorse的服务地址
		cluster.createDHorseConfig(clusterPO);
		//3.为命名空间增加镜像仓库认证key
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(globalConfigParam);
		if(globalConfigAgg != null && globalConfigAgg.getImageRepo() != null) {
			cluster.createSecret(clusterPO, globalConfigAgg.getImageRepo());
		}
		
		return null;
	}
	
	public Void delete(ClusterNamespaceDeletionParam deleteParam) {
		validateAddParam(deleteParam);
		if("default".equals(deleteParam.getNamespaceName())){
			LogUtils.throwException(logger, MessageCodeEnum.NAMESPACE_NOT_ALLOWED_DELETION);
		}
		ClusterPO clusterPO = clusterRepository.queryById(deleteParam.getClusterId());
		if (clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_EXISTENCE);
		}
		clusterStrategy(clusterPO.getClusterType()).deleteNamespace(clusterPO,
				deleteParam.getNamespaceName());
		return null;
	}
	
	private void validateAddParam(ClusterNamespaceCreationParam clusterNamespaceCreationParam) {
		if(StringUtils.isBlank(clusterNamespaceCreationParam.getClusterId())){
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		if(StringUtils.isBlank(clusterNamespaceCreationParam.getNamespaceName())){
			LogUtils.throwException(logger, MessageCodeEnum.NAMESPACE_NAME_EMPTY);
		}
	}
}
