package org.dhorse.application.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.project.member.ProjectMemberCreationParam;
import org.dhorse.api.param.project.member.ProjectMemberDeletionParam;
import org.dhorse.api.param.project.member.ProjectMemberPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.ProjectMember;
import org.dhorse.infrastructure.param.ProjectMemberParam;
import org.dhorse.infrastructure.param.SysUserParam;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.repository.po.SysUserPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 
 * 项目成员应用服务
 * 
 * @author 天地之怪
 */
@Service
public class ProjectMemberApplicationService extends BaseApplicationService<ProjectMember, ProjectMemberPO> {

	private static final Logger logger = LoggerFactory.getLogger(ProjectMemberApplicationService.class);
	
	public PageData<ProjectMember> page(LoginUser loginUser, ProjectMemberPageParam param) {
		if(!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			if(projectMemberRepository.queryByLoginNameAndProjectId(
					loginUser.getLoginName(), param.getProjectId()) == null) {
				return zeroPageData();
			}
		}
		
		IPage<ProjectMemberPO> pageData = projectMemberRepository.page(buildBizParam(param));
		if(CollectionUtils.isEmpty(pageData.getRecords())) {
			return zeroPageData();
		}
		
		//获取用户名
		List<String> userIds = pageData.getRecords().stream().map(e -> e.getUserId()).collect(Collectors.toList());
		SysUserParam sysUserParam = new SysUserParam();
		sysUserParam.setIds(userIds);
		List<SysUserPO> sysUserPO = sysUserRepository.list(sysUserParam);
		Map<String, String> idName = sysUserPO.stream().collect(Collectors.toMap(SysUserPO::getId, SysUserPO::getUserName));
		
		//具有修改（删除）操作权限的用户有两种：
		//1.管理员角色，
		//2.是该项目的管理员角色
		PageData<ProjectMember> page = pageData(pageData);
		for(ProjectMember projectUser : page.getItems()) {
			if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
				projectUser.setModifyRights(YesOrNoEnum.YES.getCode());
				projectUser.setDeleteRights(YesOrNoEnum.YES.getCode());
			}else if(projectUser.getLoginName().equals(loginUser.getLoginName())){
				List<Integer> adminRole = Constants.ROLE_OF_OPERATE_PROJECT_USER.stream()
						.filter(item -> projectUser.getRoleTypes().contains(item))
						.collect(Collectors.toList());
				if(adminRole.size() > 0) {
					projectUser.setModifyRights(YesOrNoEnum.YES.getCode());
					projectUser.setDeleteRights(YesOrNoEnum.YES.getCode());
				}
			}
			//此外，当前登录用户不具有删除自己的权限
			if(loginUser.getLoginName().equals(projectUser.getLoginName())) {
				projectUser.setDeleteRights(YesOrNoEnum.NO.getCode());
			}
			projectUser.setUserName(idName.get(projectUser.getUserId()));
		}
		return page;
	}
	
	public Void addOrUpdate(LoginUser loginUser, ProjectMemberCreationParam addParam) {
		validateParam(addParam);
		//只有管理员才有操作权限
		hasAdminRights(loginUser, addParam.getProjectId());
		if(CollectionUtils.isEmpty(addParam.getRoleTypes())){
			LogUtils.throwException(logger, MessageCodeEnum.ROLE_TYPE_IS_EMPTY);
		}
		//被添加用户必须存在
		SysUserPO sysUserPO = sysUserRepository.queryById(addParam.getUserId());
		if(sysUserPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_IS_INEXISTENCE);
		}
		//添加成员
		ProjectMemberParam bizParam = new ProjectMemberParam();
		bizParam.setProjectId(addParam.getProjectId());
		bizParam.setUserId(addParam.getUserId());
		ProjectMemberPO projectMemberPO = projectMemberRepository.query(bizParam);
		bizParam.setRoleTypes(addParam.getRoleTypes());
		if(projectMemberPO != null) {
			bizParam.setId(projectMemberPO.getId());
			projectMemberRepository.update(bizParam);
		}else {
			bizParam.setLoginName(sysUserPO.getLoginName());
			projectMemberRepository.add(bizParam);
		}
		return null;
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Void delete(LoginUser loginUser, ProjectMemberDeletionParam deleteParam) {
		validateParam(deleteParam);
		//只有管理员才有操作权限
		hasAdminRights(loginUser, deleteParam.getProjectId());
		//此外，当前登录用户不具有删除自己的权限
		if(loginUser.getId().equals(deleteParam.getUserId().toString())) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		ProjectMemberParam bizParam = buildBizParam(deleteParam);
		ProjectMemberPO projectMemberPO = projectMemberRepository.query(bizParam);
		if(projectMemberPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		projectMemberRepository.delete(projectMemberPO.getId());
		return null;
	}
	
	private void validateParam(ProjectMemberDeletionParam param) {
		if(Objects.isNull(param.getUserId())){
			LogUtils.throwException(logger, MessageCodeEnum.USER_ID_IS_EMPTY);
		}
		if(Objects.isNull(param.getProjectId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
	}
	
	private ProjectMemberParam buildBizParam(Serializable requestParam) {
		ProjectMemberParam bizParam = new ProjectMemberParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
	
	protected ProjectMember po2Dto(ProjectMemberPO po) {
		ProjectMember dto = super.po2Dto(po);
		if(Objects.isNull(po.getRoleType())) {
			return dto;
		}
		String[] roles = po.getRoleType().split(",");
		List<Integer> roleList = new ArrayList<>();
		for(String r : roles) {
			roleList.add(Integer.valueOf(r));
		}
		dto.setRoleTypes(roleList);
		return dto;
	}
}
