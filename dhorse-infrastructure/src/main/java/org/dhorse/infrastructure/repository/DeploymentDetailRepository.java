package org.dhorse.infrastructure.repository;

import org.dhorse.api.response.model.DeploymentDetail;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.repository.mapper.DeploymentDetailMapper;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.po.DeploymentDetailPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DeploymentDetailRepository
		extends RightRepository<DeploymentDetailParam, DeploymentDetailPO, DeploymentDetail> {

	@Autowired
	private DeploymentDetailMapper mapper;

	@Override
	protected CustomizedBaseMapper<DeploymentDetailPO> getMapper() {
		return mapper;
	}

	@Override
	protected DeploymentDetailPO updateCondition(DeploymentDetailParam bizParam) {
		DeploymentDetailPO po = new DeploymentDetailPO();
		po.setId(bizParam.getId());
		return po;
	}

}
