package org.dhorse.application.service;

import org.dhorse.api.enums.EnvExtTypeEnum;
import org.dhorse.api.vo.EnvExt;
import org.dhorse.api.vo.EnvHealth;
import org.dhorse.api.vo.EnvHealth.Item;
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

	public EnvHealth queryEnvHealth(LoginUser loginUser) {
		return envExtRepository.listEnvHealth();
	}
	
	public Void addOrUpdateEnvHealth(EnvHealth envHealth) {
		doAdd(envHealth.getStartup());
		doAdd(envHealth.getReadiness());
		doAdd(envHealth.getLiveness());
		return null;
	}
	
	private void doAdd(Item item) {
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
}