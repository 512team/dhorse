package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.DeploymentStatusEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.project.branch.DeploymentApplicationParam;
import org.dhorse.api.param.project.branch.deploy.DeploymentVersionDeletionParam;
import org.dhorse.api.param.project.branch.deploy.DeploymentVersionPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.DeploymentVersion;
import org.dhorse.infrastructure.param.DeployParam;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 部署版本应用服务
 * 
 * @author 天地之怪
 */
@Service
public class DeploymentVersionApplicationService extends DeployApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentVersionApplicationService.class);
	
	public PageData<DeploymentVersion> page(LoginUser loginUser, DeploymentVersionPageParam pageParam) {
		return deploymentVersionRepository.page(loginUser, buildBizParam(pageParam));
	}
	
	public Void delete(LoginUser loginUser, DeploymentVersionDeletionParam deletionParam) {
		if (StringUtils.isBlank(deletionParam.getProjectId())) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if (StringUtils.isBlank(deletionParam.getDeploymentVersionId())) {
			LogUtils.throwException(logger, MessageCodeEnum.DEPLOYEMENT_VERSION_ID_IS_EMPTY);
		}
		DeploymentVersionParam bizParam = buildBizParam(deletionParam);
		bizParam.setId(deletionParam.getDeploymentVersionId());
		deploymentVersionRepository.delete(loginUser, bizParam);
		return null;
	}

	private DeploymentVersionParam buildBizParam(Serializable requestParam) {
		DeploymentVersionParam bizParam = new DeploymentVersionParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
	
	public Void submitToDeploy(LoginUser loginUser, DeploymentApplicationParam deploymentApplictionParam) {
		if (StringUtils.isBlank(deploymentApplictionParam.getProjectId())) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		hasRights(loginUser, deploymentApplictionParam.getProjectId());
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		if (globalConfigRepository.count(globalConfigParam) < 1) {
			LogUtils.throwException(logger, MessageCodeEnum.INIT_GLOBAL_CONFIG);
		}
		ProjectEnvPO projectEnv = projectEnvRepository.queryById(deploymentApplictionParam.getEnvId());
		DeploymentVersionPO deploymentVersion = deploymentVersionRepository.queryByVersionName(
				deploymentApplictionParam.getVersionName());
		
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setDeploymentStatus(DeploymentStatusEnum.DEPLOYING_APPROVAL.getCode());
		deploymentDetailParam.setEnvId(deploymentApplictionParam.getEnvId());
		deploymentDetailParam.setVersionName(deploymentVersion.getVersionName());
		deploymentDetailParam.setBranchName(deploymentVersion.getBranchName());
		deploymentDetailParam.setProjectId(deploymentApplictionParam.getProjectId());
		deploymentDetailParam.setDeployer(loginUser.getLoginName());
		deploymentDetailParam.setStartTime(new Date());
		String deploymentDetailId = deploymentDetailRepository.add(deploymentDetailParam);

		DeployParam deployParam = new DeployParam();
		deployParam.setVersionName(deploymentVersion.getVersionName());
		deployParam.setBranchName(deploymentVersion.getBranchName());
		deployParam.setEnvId(deploymentApplictionParam.getEnvId());
		deployParam.setDeployer(loginUser.getLoginName());
		deployParam.setDeploymentDetailId(deploymentDetailId);
		deployParam.setDeploymentStartTime(deploymentDetailParam.getStartTime());
		if (YesOrNoEnum.YES.getCode().equals(projectEnv.getRequiredDeployApproval())) {
			LogUtils.throwException(logger, MessageCodeEnum.APPROVE);
		}
		deploy(deployParam);
		return null;
	}
}