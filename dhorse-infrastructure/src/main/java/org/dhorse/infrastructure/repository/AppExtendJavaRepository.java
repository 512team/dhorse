package org.dhorse.infrastructure.repository;

import org.dhorse.api.vo.AppExtendJava;
import org.dhorse.infrastructure.param.AppExtendJavaParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.AppExtendJavaMapper;
import org.dhorse.infrastructure.repository.po.AppExtendJavaPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AppExtendJavaRepository extends RightRepository<AppExtendJavaParam, AppExtendJavaPO, AppExtendJava> {

	@Autowired
	private AppExtendJavaMapper mapper;

	@Override
	protected CustomizedBaseMapper<AppExtendJavaPO> getMapper() {
		return mapper;
	}

	@Override
	protected AppExtendJavaPO updateCondition(AppExtendJavaParam bizParam) {
		AppExtendJavaPO po = new AppExtendJavaPO();
		po.setId(bizParam.getId());
		po.setAppId(bizParam.getAppId());
		return po;
	}

}