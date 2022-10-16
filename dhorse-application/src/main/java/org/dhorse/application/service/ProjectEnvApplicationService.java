package org.dhorse.application.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.project.env.ProjectEnvCreationParam;
import org.dhorse.api.param.project.env.ProjectEnvDeletionParam;
import org.dhorse.api.param.project.env.ProjectEnvPageParam;
import org.dhorse.api.param.project.env.ProjectEnvQueryParam;
import org.dhorse.api.param.project.env.ProjectEnvResoureUpdateParam;
import org.dhorse.api.param.project.env.ProjectEnvSearchParam;
import org.dhorse.api.param.project.env.ProjectEnvUpdateParam;
import org.dhorse.api.param.project.env.TraceUpdateParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.ProjectEnv;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.param.ProjectEnvParam;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.GlobalConfigPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.cluster.model.Replica;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 项目环境应用服务
 * 
 * @author 天地之怪
 */
@Service
public class ProjectEnvApplicationService extends BaseApplicationService<ProjectEnv, ProjectEnvPO> {

	private static final Logger logger = LoggerFactory.getLogger(ProjectEnvApplicationService.class);
	
	public List<ProjectEnv> search(LoginUser loginUser, ProjectEnvSearchParam param) {
		return projectEnvRepository.list(loginUser, buildBizParam(param));
	}
	
	public PageData<ProjectEnv> page(LoginUser loginUser, ProjectEnvPageParam param) {
		PageData<ProjectEnv> page = projectEnvRepository.page(loginUser, buildBizParam(param));
		if(page.getItemCount() == 0) {
			return page;
		}
		
		Map<String, ProjectPO> projectCache = new HashMap<>();
		Map<String, ClusterPO> clusterCache = new HashMap<>();
		Map<String, DeploymentVersionPO> versionCache = new HashMap<>();
		for(ProjectEnv env : page.getItems()) {
			ProjectPO projectPO = projectCache.get(env.getProjectId());
			if(projectPO == null) {
				projectPO = projectRepository.queryById(env.getProjectId());
			}
			if(projectPO == null) {
				continue;
			}
			ClusterPO clusterPO = clusterCache.get(env.getClusterId());
			if(clusterPO == null) {
				clusterPO = clusterRepository.queryById(env.getClusterId());
			}
			if(clusterPO == null) {
				continue;
			}
			ClusterStrategy clusterStrategy = clusterStrategy(clusterPO.getClusterType());
			Replica replica = null;
			try {
				replica = clusterStrategy.readDeployment(clusterPO, env, projectPO);
			}catch(Exception e) {
				logger.error("Failed to read image", e);
			}
			if(replica == null) {
				continue;
			}
			
			if(!StringUtils.isBlank(replica.getImageName())) {
				String versionName = replica.getImageName().substring(replica.getImageName().lastIndexOf("/") + 1);
				env.setVersionName(versionName);
				DeploymentVersionPO deploymentVersionPO = versionCache.get(versionName);
				if(deploymentVersionPO == null) {
					deploymentVersionPO = deploymentVersionRepository.queryByVersionName(versionName);
				}
				env.setBranchName(deploymentVersionPO != null ? deploymentVersionPO.getBranchName() : null);
			}
		}
		
//		if(CollectionUtils.isEmpty(page.getItems())) {
//			return page;
//		}
//		ClusterParam clusterParam = new ClusterParam();
//		clusterParam.setIds(page.getItems().stream().map(e -> e.getClusterId()).collect(Collectors.toList()));
//		List<ClusterPO> list = clusterRepository.list(clusterParam);
//		if (CollectionUtils.isEmpty(list)) {
//			return page;
//		}
//		Map<String, ClusterPO> clusterMap = list.stream().collect(Collectors.toMap(ClusterPO::getId, e -> e));
//		for(ProjectEnv env : page.getItems()) {
//			ClusterPO clusterPO = clusterMap.get(env.getClusterId());
//			if(clusterPO != null) {
//				env.setClusterName(clusterPO.getClusterName());
//			}
//		}
		return page;
	}
	
	public ProjectEnv query(LoginUser loginUser, ProjectEnvQueryParam queryParam) {
		if(StringUtils.isBlank(queryParam.getProjectId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if(StringUtils.isBlank(queryParam.getProjectEnvId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_ID_IS_EMPTY);
		}
		ProjectEnvParam param = new ProjectEnvParam();
		param.setProjectId(queryParam.getProjectId());
		param.setId(queryParam.getProjectEnvId());
		ProjectEnv projectEnv =  projectEnvRepository.query(loginUser, param);
		ClusterPO clusterPO = clusterRepository.queryById(projectEnv.getClusterId());
		if(clusterPO != null) {
			projectEnv.setClusterName(clusterPO.getClusterName());
		}
		if(YesOrNoEnum.YES.getCode().equals(projectEnv.getTraceStatus())) {
			GlobalConfigPO globalConfigPO = globalConfigRepository.queryById(projectEnv.getTraceTemplateId());
			if(globalConfigPO != null) {
				projectEnv.setTraceTemplateName(JsonUtils.parseToObject(globalConfigPO.getItemValue(), TraceTemplate.class).getName());
			}
		}
		return projectEnv;
	}
	
	public Void add(ProjectEnvCreationParam addParam) {
		validateAddParam(addParam);
		initAddParam(addParam);
		ProjectEnvParam param = new ProjectEnvParam();
		param.setTag(addParam.getTag());
		param.setProjectId(addParam.getProjectId());
		if(projectEnvRepository.query(param) != null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_TAG_INEXISTENCE);
		}
		if(projectEnvRepository.add(buildBizParam(addParam)) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	public Void updateTrace(LoginUser loginUser, TraceUpdateParam updateTraceParam) {
		if(StringUtils.isBlank(updateTraceParam.getProjectId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if(StringUtils.isBlank(updateTraceParam.getProjectEnvId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_ID_IS_EMPTY);
		}
		if(Objects.isNull(updateTraceParam.getTraceStatus())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_TRACE_STATUS_IS_EMPTY);
		}
		if(Objects.isNull(updateTraceParam.getTraceTemplateId())){
			LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_ID_IS_EMPTY);
		}
		
		ProjectEnvParam projectEnvParam = new ProjectEnvParam();
		projectEnvParam.setProjectId(updateTraceParam.getProjectId());
		projectEnvParam.setId(updateTraceParam.getProjectEnvId());
		projectEnvParam.setTraceStatus(updateTraceParam.getTraceStatus());
		projectEnvParam.setTraceTemplateId(updateTraceParam.getTraceTemplateId());
		if(!projectEnvRepository.update(loginUser, projectEnvParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	public Void update(LoginUser loginUser, ProjectEnvUpdateParam updateParam) {
		validateAddParam(updateParam);
		ProjectEnvParam projectEnvParam = buildBizParam(updateParam);
		projectEnvParam.setId(updateParam.getProjectEnvId());
		if(!projectEnvRepository.update(loginUser, projectEnvParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	public Void updateResource(LoginUser loginUser, ProjectEnvResoureUpdateParam updateParam) {
		if(updateParam.getProjectId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if(updateParam.getProjectEnvId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_ID_IS_EMPTY);
		}
		ProjectEnvParam projectEnvParam = buildBizParam(updateParam);
		projectEnvParam.setId(updateParam.getProjectEnvId());
		if(!projectEnvRepository.update(loginUser, projectEnvParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		ProjectPO projectPO = projectRepository.queryById(updateParam.getProjectId());
		ProjectEnvPO projectEnvPO = projectEnvRepository.queryById(updateParam.getProjectEnvId());
		ClusterPO clusterPO = clusterRepository.queryById(projectEnvPO.getClusterId());
		clusterStrategy(clusterPO.getClusterType())
			.autoScaling(projectPO, projectEnvPO, clusterPO);
		return null;
	}
	
	public Void delete(LoginUser loginUser, ProjectEnvDeletionParam deleteParam) {
		if(deleteParam.getProjectId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if(deleteParam.getProjectEnvId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_ID_IS_EMPTY);
		}
		
		//1.首先判断权限
		hasRights(loginUser, deleteParam.getProjectId());
		
		//2.再删除deployment
		ProjectPO projectPO = projectRepository.queryById(deleteParam.getProjectId());
		ProjectEnvParam bizParam = new ProjectEnvParam();
		bizParam.setProjectId(deleteParam.getProjectId());
		bizParam.setId(deleteParam.getProjectEnvId());
		ProjectEnvPO projectEnvPO = projectEnvRepository.query(bizParam);
		if(projectEnvPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_INEXISTENCE);
		}
		ClusterPO clusterPO = clusterRepository.queryById(projectEnvPO.getClusterId());
		if(clusterPO != null) {
			boolean deleteResult = clusterStrategy(clusterPO.getClusterType())
					.deleteDeployment(clusterPO, projectPO, projectEnvPO);
			if(!deleteResult) {
				LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
			}
		}
		
		//3.最后删除环境信息
		if(!projectEnvRepository.delete(deleteParam.getProjectEnvId())) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	private void validateAddParam(ProjectEnvCreationParam addParam) {
		if(StringUtils.isBlank(addParam.getEnvName())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_NAME_IS_EMPTY);
		}
		if(StringUtils.isBlank(addParam.getTag())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_TAG_IS_EMPTY);
		}
		if(StringUtils.isBlank(addParam.getProjectId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if(StringUtils.isBlank(addParam.getClusterId())){
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
		if(StringUtils.isBlank(addParam.getNamespaceName())){
			LogUtils.throwException(logger, MessageCodeEnum.NAMESPACE_NAME_EMPTY);
		}
		if(YesOrNoEnum.YES.getCode().equals(addParam.getTraceStatus())){
			if(StringUtils.isBlank(addParam.getTraceTemplateId())) {
				LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_ID_IS_EMPTY);
			}
		}else {
			addParam.setTraceTemplateId(null);
		}
		validateProject(addParam.getProjectId());
	}
	
	private void initAddParam(ProjectEnvCreationParam addParam) {
		if(Objects.isNull(addParam.getOrders())){
			addParam.setOrders(0);
		}
		if(Objects.isNull(addParam.getMinReplicas())){
			addParam.setMinReplicas(1);
		}
		if(Objects.isNull(addParam.getMaxReplicas())){
			addParam.setMaxReplicas(1);
		}
		if(Objects.isNull(addParam.getReplicaCpu())){
			addParam.setReplicaCpu(2);
		}
		if(Objects.isNull(addParam.getReplicaMemory())){
			addParam.setReplicaMemory(1024);
		}
		if(Objects.isNull(addParam.getAutoScalingCpu())){
			addParam.setAutoScalingCpu(80);
		}
		if(Objects.isNull(addParam.getAutoScalingMemory())){
			addParam.setAutoScalingMemory(80);
		}
		if(Objects.isNull(addParam.getRequiredDeployApproval())){
			addParam.setRequiredDeployApproval(0);
		}
		if(Objects.isNull(addParam.getRequiredMerge())){
			addParam.setRequiredMerge(0);
		}
		if(Objects.isNull(addParam.getTraceStatus())){
			addParam.setTraceStatus(0);
		}
	}
	
	private ProjectEnvParam buildBizParam(Serializable requestParam) {
		ProjectEnvParam bizParam = new ProjectEnvParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
}
