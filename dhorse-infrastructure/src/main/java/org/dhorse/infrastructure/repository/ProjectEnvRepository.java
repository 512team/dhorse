package org.dhorse.infrastructure.repository;

import java.util.List;

import org.dhorse.api.vo.ProjectEnv;
import org.dhorse.infrastructure.param.ProjectEnvParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.ProjectEnvMapper;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Repository
public class ProjectEnvRepository extends RightRepository<ProjectEnvParam, ProjectEnvPO, ProjectEnv> {

	@Autowired
	private ProjectEnvMapper mapper;

	@Override
	protected CustomizedBaseMapper<ProjectEnvPO> getMapper() {
		return mapper;
	}

	public List<ProjectEnvPO> list(List<String> projectIds) {
		QueryWrapper<ProjectEnvPO> wrapper = new QueryWrapper<>();
		wrapper.in("project_id", projectIds);
		wrapper.eq("deletion_status", 0);
		return mapper.selectList(wrapper);
	}
	
	@Override
	protected ProjectEnvPO updateCondition(ProjectEnvParam bizParam) {
		ProjectEnvPO po = new ProjectEnvPO();
		po.setId(bizParam.getId());
		return po;
	}

}
