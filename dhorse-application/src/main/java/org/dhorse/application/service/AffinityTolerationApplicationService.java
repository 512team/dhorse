package org.dhorse.application.service;

import java.io.Serializable;

import org.dhorse.infrastructure.utils.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationCreationParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationDeletionParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationPageParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationQueryParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationUpdateParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AffinityToleration;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.param.AffinityTolerationParam;
import org.dhorse.infrastructure.repository.po.AffinityTolerationPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 亲和容忍配置应用服务
 * 
 * @author 天地之怪
 */
@Service
public class AffinityTolerationApplicationService
		extends BaseApplicationService<AffinityToleration, AffinityTolerationPO> {

	private static final Logger logger = LoggerFactory.getLogger(AffinityTolerationApplicationService.class);

	public PageData<AffinityToleration> page(LoginUser loginUser, AffinityTolerationPageParam param) {
		return affinityTolerationRepository.page(loginUser, buildBizParam(param));
	}
	
	public AffinityToleration query(LoginUser loginUser, AffinityTolerationQueryParam param) {
		AffinityTolerationParam bizParam = buildBizParam(param);
		bizParam.setId(param.getAffinityTolerationId());
		return affinityTolerationRepository.query(loginUser, bizParam);
	}

	public Void add(AffinityTolerationCreationParam addParam) {
		validateAddParam(addParam);
		AffinityTolerationParam bizParam = buildBizParam(addParam);
		if (affinityTolerationRepository.add(bizParam) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}

	public Void update(LoginUser loginUser, AffinityTolerationUpdateParam updateParam) {
		validateAddParam(updateParam);
		AffinityTolerationParam bizParam = buildBizParam(updateParam);
		bizParam.setId(updateParam.getAffinityTolerationId());
		if (!affinityTolerationRepository.update(bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}

	public Void openStatus(LoginUser loginUser, AffinityTolerationUpdateParam updateParam) {
		AffinityTolerationParam bizParam = buildBizParam(updateParam);
		bizParam.setId(updateParam.getAffinityTolerationId());
		if (!affinityTolerationRepository.update(bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	public Void delete(LoginUser loginUser, AffinityTolerationDeletionParam deleteParam) {
		if (StringUtils.isBlank(deleteParam.getAffinityTolerationId())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "亲和容忍配置编号不能为空");
		}
		AffinityTolerationParam bizParam = new AffinityTolerationParam();
		bizParam.setId(deleteParam.getAffinityTolerationId());
		bizParam.setAppId(deleteParam.getAppId());
		bizParam.setEnvId(deleteParam.getEnvId());
		if (!affinityTolerationRepository.delete(loginUser, bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}

	private void validateAddParam(AffinityTolerationCreationParam addParam) {
		if (StringUtils.isBlank(addParam.getEnvId())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ENV_TAG_IS_EMPTY);
		}
		if (StringUtils.isBlank(addParam.getAppId())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
		if (addParam.getSchedulingType() == null) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "调度类型不能为空");
		}
		if (StringUtils.isBlank(addParam.getKeyName())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "键不能为空");
		}
		if (StringUtils.isBlank(addParam.getOperator())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "操作符不能为空");
		}
	}

	private AffinityTolerationParam buildBizParam(Serializable requestParam) {
		AffinityTolerationParam bizParam = new AffinityTolerationParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
}
