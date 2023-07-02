package org.dhorse.application.service;

import org.dhorse.api.enums.EnvExtTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.param.app.env.EnvHealthQueryParam;
import org.dhorse.api.param.app.env.EnvLifeCycleQueryParam;
import org.dhorse.api.param.app.env.EnvPrometheusQueryParam;
import org.dhorse.api.response.model.EnvExt;
import org.dhorse.api.response.model.EnvHealth;
import org.dhorse.api.response.model.EnvLifecycle;
import org.dhorse.api.response.model.EnvPrometheus;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.param.EnvExtParam;
import org.dhorse.infrastructure.repository.po.EnvExtPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * 
 * 环境扩展服务
 * 
 * @author Dahai
 */
@Service
public class EnvExtApplicationService extends BaseApplicationService<EnvExt, EnvExtPO> {

	public EnvHealth queryEnvHealth(LoginUser loginUser, EnvHealthQueryParam queryParam) {
		this.hasRights(loginUser, queryParam.getAppId());
		EnvExtParam bizParam = new EnvExtParam();
		bizParam.setAppId(queryParam.getAppId());
		bizParam.setEnvId(queryParam.getEnvId());
		bizParam.setExType(EnvExtTypeEnum.HEALTH.getCode());
		return envExtRepository.listEnvHealth(bizParam);
	}
	
	public Void addOrUpdateEnvHealth(LoginUser loginUser, EnvHealth envHealth) {
		this.hasAdminRights(loginUser, envHealth.getStartup().getAppId());
		addOrUpdateEnvHealthItem(envHealth.getStartup());
		addOrUpdateEnvHealthItem(envHealth.getReadiness());
		addOrUpdateEnvHealthItem(envHealth.getLiveness());
		return null;
	}
	
	private void addOrUpdateEnvHealthItem(EnvHealth.Item item) {
		if(item.getActionType() != null && StringUtils.isBlank(item.getAction())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "检查内容不能为空");
		}
		EnvExtParam ext = new EnvExtParam();
		ext.setAppId(item.getAppId());
		ext.setEnvId(item.getEnvId());
		ext.setExType(EnvExtTypeEnum.HEALTH.getCode());
		ext.setExt(JsonUtils.toJsonString(item, "id", "appId", "envId"));
		if(StringUtils.isBlank(item.getId())){
			envExtRepository.add(ext);
		}else {
			ext.setId(item.getId());
			envExtRepository.updateById(ext);
		}
	}
	
	public EnvLifecycle queryLifecycle(LoginUser loginUser, EnvLifeCycleQueryParam queryParam) {
		this.hasRights(loginUser, queryParam.getAppId());
		EnvExtParam bizParam = new EnvExtParam();
		bizParam.setAppId(queryParam.getAppId());
		bizParam.setEnvId(queryParam.getEnvId());
		bizParam.setExType(EnvExtTypeEnum.LIFECYCLE.getCode());
		return envExtRepository.listLifecycle(bizParam);
	}
	
	public Void addOrUpdateLifecycle(LoginUser loginUser, EnvLifecycle envLifecycle) {
		this.hasAdminRights(loginUser, envLifecycle.getPostStart().getAppId());
		addOrUpdateLifecycleItem(envLifecycle.getPostStart());
		addOrUpdateLifecycleItem(envLifecycle.getPreStop());
		return null;
	}
	
	private void addOrUpdateLifecycleItem(EnvLifecycle.Item item) {
		if(item.getActionType() != null && StringUtils.isBlank(item.getAction())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "执行内容不能为空");
		}
		EnvExtParam ext = new EnvExtParam();
		ext.setAppId(item.getAppId());
		ext.setEnvId(item.getEnvId());
		ext.setExType(EnvExtTypeEnum.LIFECYCLE.getCode());
		ext.setExt(JsonUtils.toJsonString(item, "id", "appId", "envId"));
		if(StringUtils.isBlank(item.getId())){
			envExtRepository.add(ext);
		}else {
			ext.setId(item.getId());
			envExtRepository.updateById(ext);
		}
	}
	
	public EnvPrometheus queryPrometheus(LoginUser loginUser, EnvPrometheusQueryParam queryParam) {
		this.hasRights(loginUser, queryParam.getAppId());
		EnvExtParam bizParam = new EnvExtParam();
		bizParam.setAppId(queryParam.getAppId());
		bizParam.setEnvId(queryParam.getEnvId());
		bizParam.setExType(EnvExtTypeEnum.PROMETHEUS.getCode());
		return envExtRepository.queryPrometheus(bizParam);
	}
	
	public Void addOrUpdatePrometheus(LoginUser loginUser, EnvPrometheus addParam) {
		if(StringUtils.isEmpty(addParam.getKind())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "采集维度不能为空");
		}
		EnvExtParam ext = new EnvExtParam();
		ext.setAppId(addParam.getAppId());
		ext.setEnvId(addParam.getEnvId());
		ext.setExType(EnvExtTypeEnum.PROMETHEUS.getCode());
		ext.setExt(JsonUtils.toJsonString(addParam, "id", "appId", "envId", "modifyRights", "deleteRights"));
		if(StringUtils.isBlank(addParam.getId())){
			envExtRepository.add(ext);
		}else {
			ext.setId(addParam.getId());
			envExtRepository.updateById(ext);
		}
		return null;
	}
}