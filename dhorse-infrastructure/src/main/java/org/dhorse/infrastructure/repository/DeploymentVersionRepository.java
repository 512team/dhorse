package org.dhorse.infrastructure.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.DeploymentVersion;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.DeploymentVersionMapper;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

@Repository
public class DeploymentVersionRepository
		extends RightRepository<DeploymentVersionParam, DeploymentVersionPO, DeploymentVersion> {

	@Autowired
	private DeploymentVersionMapper mapper;
	
	@Autowired
	private AppEnvRepository envRepository;
	
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
		//使用列表参数查询，不使用环境编号
		bizParam.setEnvIds(envIds);
		bizParam.setEnvId(null);
		return super.page(loginUser, bizParam);
	}
	
	@Override
	protected List<DeploymentVersion> pos2Dtos(List<DeploymentVersionPO> pos) {
		if(CollectionUtils.isEmpty(pos)) {
			return null;
		}
		List<String> envIds = pos.stream().map(e -> e.getEnvId()).collect(Collectors.toList());
		AppEnvParam envParam = new AppEnvParam();
		envParam.setIds(envIds);
		List<AppEnvPO> envs = envRepository.list(envParam);
		if(CollectionUtils.isEmpty(envs)) {
			return super.pos2Dtos(pos);
		}
		Map<String, AppEnvPO> envMap = envs.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
		List<DeploymentVersion> views = super.pos2Dtos(pos);
		for(DeploymentVersion view : views) {
			AppEnvPO env = envMap.get(view.getEnvId());
			if(env != null) {
				view.setEnvName(env.getEnvName());
			}
		}
		return views;
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
