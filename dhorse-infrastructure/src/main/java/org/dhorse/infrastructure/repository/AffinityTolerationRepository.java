package org.dhorse.infrastructure.repository;

import org.dhorse.api.response.model.AffinityToleration;
import org.dhorse.infrastructure.param.AffinityTolerationParam;
import org.dhorse.infrastructure.repository.mapper.AffinityTolerationMapper;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.po.AffinityTolerationPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AffinityTolerationRepository extends RightRepository<AffinityTolerationParam, AffinityTolerationPO, AffinityToleration> {

	@Autowired
	private AffinityTolerationMapper mapper;
	
	@Override
	protected CustomizedBaseMapper<AffinityTolerationPO> getMapper() {
		return mapper;
	}

	@Override
	protected AffinityTolerationPO updateCondition(AffinityTolerationParam bizParam) {
		AffinityTolerationPO po = new AffinityTolerationPO();
		po.setId(bizParam.getId());
		return po;
	}
}