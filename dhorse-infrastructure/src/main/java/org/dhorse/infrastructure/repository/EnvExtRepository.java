package org.dhorse.infrastructure.repository;

import java.util.List;

import org.dhorse.api.response.model.EnvAutoDeployment;
import org.dhorse.api.response.model.EnvExt;
import org.dhorse.api.response.model.EnvHealth;
import org.dhorse.api.response.model.EnvLifecycle;
import org.dhorse.api.response.model.EnvPrometheus;
import org.dhorse.infrastructure.param.EnvExtParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.EnvExtMapper;
import org.dhorse.infrastructure.repository.po.EnvExtPO;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public class EnvExtRepository extends RightRepository<EnvExtParam, EnvExtPO, EnvExt> {

	@Autowired
	private EnvExtMapper mapper;
	
	@Override
	protected CustomizedBaseMapper<EnvExtPO> getMapper() {
		return mapper;
	}

	public EnvHealth listEnvHealth(EnvExtParam bizParam) {
		List<EnvExtPO> pos = this.list(bizParam);
		EnvHealth envHealth = new EnvHealth();
		if(CollectionUtils.isEmpty(pos)) {
			return envHealth;
		}
		for(EnvExtPO po : pos) {
			EnvHealth.Item one = JsonUtils.parseToObject(po.getExt(), EnvHealth.Item.class);
			one.setId(po.getId());
			one.setAppId(po.getAppId());
			one.setEnvId(po.getEnvId());
			if(EnvHealth.HealthTypeEnum.STARTUP.getCode().equals(one.getHealthType())) {
				envHealth.setStartup(one);
			}else if(EnvHealth.HealthTypeEnum.READINESS.getCode().equals(one.getHealthType())) {
				envHealth.setReadiness(one);
			}else if(EnvHealth.HealthTypeEnum.LIVENESS.getCode().equals(one.getHealthType())) {
				envHealth.setLiveness(one);
			}
		}
		return envHealth;
	}
	
	public EnvLifecycle listLifecycle(EnvExtParam bizParam) {
		List<EnvExtPO> pos = this.list(bizParam);
		EnvLifecycle model = new EnvLifecycle();
		if(CollectionUtils.isEmpty(pos)) {
			return model;
		}
		for(EnvExtPO po : pos) {
			EnvLifecycle.Item one = JsonUtils.parseToObject(po.getExt(), EnvLifecycle.Item.class);
			one.setId(po.getId());
			one.setAppId(po.getAppId());
			one.setEnvId(po.getEnvId());
			if(EnvLifecycle.HookTypeEnum.POST_START.getCode().equals(one.getHookType())) {
				model.setPostStart(one);
			}else if(EnvLifecycle.HookTypeEnum.PRE_STOP.getCode().equals(one.getHookType())) {
				model.setPreStop(one);
			}
		}
		return model;
	}
	
	public EnvPrometheus queryPrometheus(EnvExtParam bizParam) {
		EnvExtPO po = this.query(bizParam);
		if(po == null) {
			return null;
		}
		EnvPrometheus model = JsonUtils.parseToObject(po.getExt(), EnvPrometheus.class);
		model.setId(po.getId());
		model.setAppId(po.getAppId());
		model.setEnvId(po.getEnvId());
		return model;
	}
	
	public EnvAutoDeployment queryEnvAutoDeployment(EnvExtParam bizParam) {
		EnvExtPO po = this.query(bizParam);
		if(po == null) {
			return null;
		}
		EnvAutoDeployment model = JsonUtils.parseToObject(po.getExt(), EnvAutoDeployment.class);
		model.setId(po.getId());
		model.setAppId(po.getAppId());
		model.setEnvId(po.getEnvId());
		return model;
	}
	
	@Override
	protected EnvExtPO updateCondition(EnvExtParam bizParam) {
		EnvExtPO po = new EnvExtPO();
		po.setId(bizParam.getId());
		return po;
	}
}