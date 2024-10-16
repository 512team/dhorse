package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import org.dhorse.infrastructure.utils.StringUtils;
import org.dhorse.api.enums.DeploymentStatusEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.app.branch.deploy.DeploymentParam;
import org.dhorse.api.param.app.branch.deploy.DeploymentVersionDeletionParam;
import org.dhorse.api.param.app.branch.deploy.DeploymentVersionPageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.DeploymentVersion;
import org.dhorse.infrastructure.param.DeployParam;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.DeploymentDetailPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
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
public class DeploymentVersionApplicationService extends DeploymentApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentVersionApplicationService.class);
	
	public PageData<DeploymentVersion> page(LoginUser loginUser, DeploymentVersionPageParam pageParam) {
		return deploymentVersionRepository.page(loginUser, buildBizParam(pageParam));
	}
	
	public PageData<DeploymentVersion> search(LoginUser loginUser, DeploymentVersionPageParam pageParam) {
		return deploymentVersionRepository.search(loginUser, buildBizParam(pageParam));
	}
	
	public Void delete(LoginUser loginUser, DeploymentVersionDeletionParam deletionParam) {
		if (StringUtils.isBlank(deletionParam.getAppId())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
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
	
	public Void submitToDeploy(LoginUser loginUser, DeploymentParam deploymentParam) {
		if (StringUtils.isBlank(deploymentParam.getAppId())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
		hasRights(loginUser, deploymentParam.getAppId());
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		if (globalConfigRepository.count(globalConfigParam) < 1) {
			LogUtils.throwException(logger, MessageCodeEnum.INIT_GLOBAL_CONFIG);
		}
		AppEnvPO appEnv = appEnvRepository.queryById(deploymentParam.getEnvId());
		DeploymentVersionPO deploymentVersion = deploymentVersionRepository.queryByVersionName(
				deploymentParam.getVersionName());
		
		// 当前环境是否存在部署中
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setEnvId(deploymentParam.getEnvId());
		deploymentDetailParam.setDeploymentStatuss(Arrays.asList(DeploymentStatusEnum.DEPLOYING.getCode(),
				DeploymentStatusEnum.ROLLBACKING.getCode()));
		DeploymentDetailPO deploymentDetailPO = deploymentDetailRepository.query(deploymentDetailParam);
		if (deploymentDetailPO != null) {
			LogUtils.throwException(logger, MessageCodeEnum.ENV_DEPLOYING);
		}
		
		deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setDeploymentStatus(DeploymentStatusEnum.DEPLOYING_APPROVAL.getCode());
		deploymentDetailParam.setEnvId(deploymentParam.getEnvId());
		deploymentDetailParam.setVersionName(deploymentVersion.getVersionName());
		deploymentDetailParam.setBranchName(deploymentVersion.getBranchName());
		deploymentDetailParam.setAppId(deploymentParam.getAppId());
		deploymentDetailParam.setDeployer(loginUser.getLoginName());
		deploymentDetailParam.setStartTime(new Date());
		String deploymentDetailId = deploymentDetailRepository.add(deploymentDetailParam);

		DeployParam deployParam = new DeployParam();
		deployParam.setVersionName(deploymentVersion.getVersionName());
		deployParam.setBranchName(deploymentVersion.getBranchName());
		deployParam.setEnvId(deploymentParam.getEnvId());
		deployParam.setDeployer(loginUser.getLoginName());
		deployParam.setDeploymentDetailId(deploymentDetailId);
		deployParam.setDeploymentStartTime(deploymentDetailParam.getStartTime());
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())
				&& YesOrNoEnum.YES.getCode().equals(appEnv.getRequiredDeployApproval())) {
			LogUtils.throwException(logger, MessageCodeEnum.APPROVE);
		}
		deploy(deployParam);
		return null;
	}
}