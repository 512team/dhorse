package org.dhorse.infrastructure.repository;

import org.dhorse.api.vo.DeploymentVersion;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.DeploymentVersionMapper;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DeploymentVersionRepository
		extends RightRepository<DeploymentVersionParam, DeploymentVersionPO, DeploymentVersion> {

	@Autowired
	private DeploymentVersionMapper mapper;
	
	public DeploymentVersionPO queryByVersionName(String versionName) {
		DeploymentVersionParam bizParam = new DeploymentVersionParam();
		bizParam.setVersionName(versionName);
		return this.query(bizParam);
	}

	@Override
	protected CustomizedBaseMapper<DeploymentVersionPO> getMapper() {
		return mapper;
	}

	@Override
	protected DeploymentVersionPO updateCondition(DeploymentVersionParam bizParam) {
		DeploymentVersionPO po = new DeploymentVersionPO();
		po.setId(bizParam.getId());
		return po;
	}

}
