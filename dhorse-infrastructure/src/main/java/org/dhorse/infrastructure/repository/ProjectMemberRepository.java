package org.dhorse.infrastructure.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.dhorse.infrastructure.param.ProjectMemberParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.ProjectMemberMapper;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.utils.QueryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

@Repository
public class ProjectMemberRepository
		extends BaseRepository<ProjectMemberParam, ProjectMemberPO> {

	@Autowired
	private ProjectMemberMapper mapper;

	public boolean deleteByUserId(String userId) {
		ProjectMemberPO po = new ProjectMemberPO();
		po.setUserId(userId);
		UpdateWrapper<ProjectMemberPO> wrapper = QueryHelper.buildUpdateWrapper(po);
		po.setDeletionStatus(1);
		return getMapper().update(po, wrapper) > 0 ? true : false;
	}
	
	public boolean deleteByProjectId(String projectId) {
		ProjectMemberPO po = new ProjectMemberPO();
		po.setProjectId(projectId);
		UpdateWrapper<ProjectMemberPO> wrapper = QueryHelper.buildUpdateWrapper(po);
		po.setDeletionStatus(1);
		return getMapper().update(po, wrapper) > 0 ? true : false;
	}
	
	public List<ProjectMemberPO> queryByLoginName(String loginName) {
		ProjectMemberParam projectMemberParam = new ProjectMemberParam();
		projectMemberParam.setLoginName(loginName);
		return super.list(projectMemberParam);
	}
	
	public ProjectMemberPO queryByLoginNameAndProjectId(String loginName, String projectId) {
		ProjectMemberParam projectMemberParam = new ProjectMemberParam();
		projectMemberParam.setLoginName(loginName);
		projectMemberParam.setProjectId(projectId);
		return super.query(projectMemberParam);
	}
	
	@Override
	protected CustomizedBaseMapper<ProjectMemberPO> getMapper() {
		return mapper;
	}

	@Override
	protected ProjectMemberPO updateCondition(ProjectMemberParam bizParam) {
		ProjectMemberPO po = new ProjectMemberPO();
		po.setId(bizParam.getId());
		po.setUserId(bizParam.getUserId());
		return po;
	}
	
	protected ProjectMemberPO param2Entity(ProjectMemberParam bizParam) {
		ProjectMemberPO po = super.param2Entity(bizParam);
		if(CollectionUtils.isEmpty(bizParam.getRoleTypes())) {
			return po;
		}
		po.setRoleType(bizParam.getRoleTypes().stream().map(e -> e.toString()).collect(Collectors.joining(",")));
		return po;
	}
}
