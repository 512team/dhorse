package org.dhorse.infrastructure.repository;

import java.util.List;

import org.dhorse.api.enums.EnvExtTypeEnum;
import org.dhorse.api.vo.EnvExt;
import org.dhorse.api.vo.EnvHealth;
import org.dhorse.api.vo.EnvHealth.Item;
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

	public EnvHealth listEnvHealth() {
		EnvExtParam bizParam = new EnvExtParam();
		bizParam.setExType(EnvExtTypeEnum.HEALTH.getCode());
		List<EnvExtPO> pos = this.list(bizParam);
		EnvHealth envHealth = new EnvHealth();
		if(CollectionUtils.isEmpty(pos)) {
			return envHealth;
		}
		for(EnvExtPO po : pos) {
			Item one = JsonUtils.parseToObject(po.getExt(), Item.class);
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
	
	@Override
	protected EnvExtPO updateCondition(EnvExtParam bizParam) {
		EnvExtPO po = new EnvExtPO();
		po.setId(bizParam.getId());
		return po;
	}
}