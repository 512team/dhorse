package org.dhorse.infrastructure.repository;

import org.dhorse.api.vo.ProjectExtendJava;
import org.dhorse.infrastructure.param.ProjectExtendJavaParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.ProjectExtendJavaMapper;
import org.dhorse.infrastructure.repository.po.ProjectExtendJavaPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectExtendJavaRepository extends RightRepository<ProjectExtendJavaParam, ProjectExtendJavaPO, ProjectExtendJava> {

	@Autowired
	private ProjectExtendJavaMapper mapper;

	@Override
	protected CustomizedBaseMapper<ProjectExtendJavaPO> getMapper() {
		return mapper;
	}

	@Override
	protected ProjectExtendJavaPO updateCondition(ProjectExtendJavaParam bizParam) {
		ProjectExtendJavaPO po = new ProjectExtendJavaPO();
		po.setId(bizParam.getId());
		po.setProjectId(bizParam.getProjectId());
		return po;
	}

}