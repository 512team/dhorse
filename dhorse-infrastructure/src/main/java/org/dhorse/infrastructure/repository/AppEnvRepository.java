package org.dhorse.infrastructure.repository;

import java.util.List;

import org.dhorse.api.vo.AppEnv;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.AppEnvMapper;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Repository
public class AppEnvRepository extends RightRepository<AppEnvParam, AppEnvPO, AppEnv> {

	@Autowired
	private AppEnvMapper mapper;

	@Override
	protected CustomizedBaseMapper<AppEnvPO> getMapper() {
		return mapper;
	}

	public List<AppEnvPO> list(List<String> appIds) {
		QueryWrapper<AppEnvPO> wrapper = new QueryWrapper<>();
		wrapper.in("app_id", appIds);
		wrapper.eq("deletion_status", 0);
		return mapper.selectList(wrapper);
	}
	
	@Override
	protected AppEnvPO updateCondition(AppEnvParam bizParam) {
		AppEnvPO po = new AppEnvPO();
		po.setId(bizParam.getId());
		return po;
	}

}
