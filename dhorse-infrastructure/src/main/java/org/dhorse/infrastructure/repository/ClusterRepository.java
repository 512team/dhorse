package org.dhorse.infrastructure.repository;

import org.dhorse.infrastructure.param.ClusterParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.ClusterMapper;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClusterRepository
		extends BaseRepository<ClusterParam, ClusterPO> {

	@Autowired
	private ClusterMapper mapper;

	@Override
	protected CustomizedBaseMapper<ClusterPO> getMapper() {
		return mapper;
	}

	@Override
	protected ClusterPO updateCondition(ClusterParam bizParam) {
		ClusterPO po = new ClusterPO();
		po.setId(bizParam.getId());
		return po;
	}

}