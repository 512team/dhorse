package org.dhorse.infrastructure.repository;

import java.util.ArrayList;
import java.util.List;

import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.DeploymentVersion;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.DeploymentVersionMapper;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

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
	
	/**
	 * 当没有选择环境进行构建版本时，设置一个默认的环境编号0，
	 * 这种没有和环境关联的版本，在所有环境发布页面都应该展示处理
	 * 见：search方法
	 */
	@Override
	public String add(DeploymentVersionParam bizParam) {
		if(StringUtils.isBlank(bizParam.getEnvId())) {
			bizParam.setEnvId("0");
		}
		return super.add(bizParam);
	}

	/**
	 * 查询时，要把非环境相关的所有版本都返回
	 * 见：add方法
	 */
	public PageData<DeploymentVersion> search(LoginUser loginUser, DeploymentVersionParam bizParam) {
		List<String> envIds = new ArrayList<>();
		envIds.add("0");
		if(!StringUtils.isBlank(bizParam.getEnvId())) {
			envIds.add(bizParam.getEnvId());
		}
		bizParam.setEnvId(null);
		bizParam.setEnvIds(envIds);
		return super.page(loginUser, bizParam);
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
