package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.DeploymentStatusEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.project.branch.deploy.DeploymentApprovementParam;
import org.dhorse.api.param.project.branch.deploy.DeploymentDetailDeletionParam;
import org.dhorse.api.param.project.branch.deploy.DeploymentDetailPageParam;
import org.dhorse.api.param.project.branch.deploy.RollbackApplicationParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.DeploymentDetail;
import org.dhorse.api.vo.ProjectEnv;
import org.dhorse.infrastructure.param.DeployParam;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentDetailPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.cluster.model.Replica;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 
 * 部署历史应用服务
 * 
 * @author 天地之怪
 */
@Service
public class DeploymentDetailApplicationService extends DeployApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentDetailApplicationService.class);

	public PageData<DeploymentDetail> page(LoginUser loginUser, DeploymentDetailPageParam param) {
		PageData<DeploymentDetail> page = deploymentDetailRepository.page(loginUser, buildBizParam(param));
		if(CollectionUtils.isEmpty(page.getItems())) {
			return page;
		}
		
		ProjectEnvPO projectEnvPO = projectEnvRepository.queryById(param.getProjectEnvId());
		if (projectEnvPO == null) {
			return page;
		}
		
		ProjectMemberPO projectUser = projectMemberRepository
				.queryByLoginNameAndProjectId(loginUser.getLoginName(), param.getProjectId());
		String[] roleTypes = projectUser.getRoleType().split(",");
		Set<Integer> roleSet = new HashSet<>();
		for (String role : roleTypes) {
			roleSet.add(Integer.valueOf(role));
		}
		String currentVersionName = currentVersionName(projectEnvPO);
		for(DeploymentDetail e : page.getItems()) {
			e.setEnvName(projectEnvPO.getEnvName());
			//审批权限
			if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
				e.setDeployApprovalRights(YesOrNoEnum.YES.getCode());
				e.setRollbackApprovalRights(YesOrNoEnum.YES.getCode());
			} else {
				List<Integer> adminRole = Constants.ROLE_OF_OPERATE_PROJECT_USER.stream()
						.filter(item -> roleSet.contains(item))
						.collect(Collectors.toList());
				if(adminRole.size() > 0) {
					e.setDeployApprovalRights(YesOrNoEnum.YES.getCode());
					e.setRollbackApprovalRights(YesOrNoEnum.YES.getCode());
				}
			}
			if(!DeploymentStatusEnum.DEPLOYING_APPROVAL.getCode().equals(e.getDeploymentStatus())) {
				e.setDeployApprovalRights(YesOrNoEnum.NO.getCode());
			}
			if(!DeploymentStatusEnum.ROLLBACK_APPROVAL.getCode().equals(e.getDeploymentStatus())) {
				e.setRollbackApprovalRights(YesOrNoEnum.NO.getCode());
			}
			e.setRollbackRights(DeploymentStatusEnum.ROLLBACK_FAILURE.getCode().equals(e.getDeploymentStatus())
					|| (canRollback(e.getDeploymentStatus()) && !e.getVersionName().equals(currentVersionName)) ? 1 : 0);
		}
		return page;
	}
	
	private boolean canRollback(Integer deploymentStatus) {
		boolean show = DeploymentStatusEnum.DEPLOYED_SUCCESS.getCode().equals(deploymentStatus)
				|| DeploymentStatusEnum.MERGED_SUCCESS.getCode().equals(deploymentStatus)
				|| DeploymentStatusEnum.MERGED_FAILURE.getCode().equals(deploymentStatus);
		return show;
	}

	private String currentVersionName(ProjectEnvPO projectEnvPO) {
		ProjectPO projectPO = projectRepository.queryById(projectEnvPO.getProjectId());
		ClusterPO clusterPO = clusterRepository.queryById(projectEnvPO.getClusterId());
		ClusterStrategy clusterStrategy = clusterStrategy(clusterPO.getClusterType());
		ProjectEnv projectEnv = new ProjectEnv();
		projectEnv.setNamespaceName(projectEnvPO.getNamespaceName());
		projectEnv.setTag(projectEnvPO.getTag());
		Replica replica = null;
		try {
			replica = clusterStrategy.readDeployment(clusterPO, projectEnv, projectPO);
		}catch(Exception e) {
			logger.error("Failed to read image", e);
			return null;
		}
		if(replica == null) {
			return null;
		}
		return replica.getImageName().substring(replica.getImageName().lastIndexOf("/") + 1);
	}
	
	public Void approveToDeploy(LoginUser loginUser, DeploymentApprovementParam requestParam) {
		DeploymentDetailPO deploymentDetailPO = deployParamValidate(loginUser, requestParam);
		if (!DeploymentStatusEnum.DEPLOYING_APPROVAL.getCode().equals(deploymentDetailPO.getDeploymentStatus())
				&& !DeploymentStatusEnum.ROLLBACK_APPROVAL.getCode().equals(deploymentDetailPO.getDeploymentStatus())) {
			LogUtils.throwException(logger, MessageCodeEnum.DEPLOYING_BRANCH_IS_APPROVED);
		}
		DeployParam deployParam = new DeployParam();
		deployParam.setApprover(loginUser.getLoginName());
		deployParam.setVersionName(deploymentDetailPO.getVersionName());
		deployParam.setBranchName(deploymentDetailPO.getBranchName());
		deployParam.setEnvId(deploymentDetailPO.getEnvId());
		deployParam.setDeploymentDetailId(deploymentDetailPO.getId());
		deployParam.setDeploymentStartTime(new Date());
		if(DeploymentStatusEnum.DEPLOYING_APPROVAL.getCode().equals(deploymentDetailPO.getDeploymentStatus())) {
			deploy(deployParam);
		}else if(DeploymentStatusEnum.ROLLBACK_APPROVAL.getCode().equals(deploymentDetailPO.getDeploymentStatus())) {
			rollback(deployParam);
		}
		
		return null;
	}

	public Void submitToRollback(LoginUser loginUser, RollbackApplicationParam requestParam) {
		DeploymentDetailPO deploymentDetailPO = deployParamValidate(loginUser, requestParam);
		if (!canRollback(deploymentDetailPO.getDeploymentStatus())
				&& !DeploymentStatusEnum.ROLLBACK_FAILURE.getCode().equals(deploymentDetailPO.getDeploymentStatus())) {
			LogUtils.throwException(logger, MessageCodeEnum.DEPLOYED_STATUS_NOT_ROLLBACK);
		}
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setId(deploymentDetailPO.getId());
		deploymentDetailParam.setDeployer(loginUser.getLoginName());
		// 需要审核
		ProjectEnvPO projectEnv = projectEnvRepository.queryById(deploymentDetailPO.getEnvId());
		if (YesOrNoEnum.YES.getCode().equals(projectEnv.getRequiredDeployApproval())) {
			deploymentDetailParam.setDeploymentStatus(DeploymentStatusEnum.ROLLBACK_APPROVAL.getCode());
			deploymentDetailRepository.update(deploymentDetailParam);
			LogUtils.throwException(logger, MessageCodeEnum.APPROVE);
		}
		DeployParam deployParam = new DeployParam();
		deployParam.setApprover(loginUser.getLoginName());
		deployParam.setVersionName(deploymentDetailPO.getVersionName());
		deployParam.setBranchName(deploymentDetailPO.getBranchName());
		deployParam.setEnvId(deploymentDetailPO.getEnvId());
		deployParam.setDeploymentDetailId(deploymentDetailPO.getId());
		deployParam.setDeploymentStartTime(new Date());
		
		// 回滚
		rollback(deployParam);

		return null;
	}

	private DeploymentDetailPO deployParamValidate(LoginUser loginUser, DeploymentApprovementParam requestParam) {
		if (StringUtils.isBlank(requestParam.getProjectId())) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if (StringUtils.isBlank(requestParam.getDeploymentDetailId())) {
			LogUtils.throwException(logger, MessageCodeEnum.BRANCH_DEPLOYED_DETAIL_ID_IS_EMPTY);
		}
		hasRights(loginUser, requestParam.getProjectId());
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setProjectId(requestParam.getProjectId());
		deploymentDetailParam.setId(requestParam.getDeploymentDetailId());
		DeploymentDetailPO deploymentDetailPO = deploymentDetailRepository.query(deploymentDetailParam);
		if (deploymentDetailPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		return deploymentDetailPO;
	}

	public Void delete(LoginUser loginUser, DeploymentDetailDeletionParam requestParam) {
		DeploymentDetailPO deploymentDetailPO = deployParamValidate(loginUser, requestParam);
		deploymentDetailRepository.delete(deploymentDetailPO.getId());
		return null;
	}

	private DeploymentDetailParam buildBizParam(Serializable requestParam) {
		DeploymentDetailParam bizParam = new DeploymentDetailParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
}