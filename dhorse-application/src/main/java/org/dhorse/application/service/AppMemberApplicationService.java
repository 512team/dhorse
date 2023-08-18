package org.dhorse.application.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.dhorse.infrastructure.utils.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RegisteredSourceEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.app.member.AppMemberCreationParam;
import org.dhorse.api.param.app.member.AppMemberDeletionParam;
import org.dhorse.api.param.app.member.AppMemberPageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppMember;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.SysUserParam;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
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
 * 应用成员应用服务
 * 
 * @author 天地之怪
 */
@Service
public class AppMemberApplicationService extends BaseApplicationService<AppMember, AppMemberPO> {

	private static final Logger logger = LoggerFactory.getLogger(AppMemberApplicationService.class);
	
	public PageData<AppMember> page(LoginUser loginUser, AppMemberPageParam param) {
		if(!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			if(appMemberRepository.queryByLoginNameAndAppId(
					loginUser.getLoginName(), param.getAppId()) == null) {
				return zeroPageData();
			}
		}
		
		IPage<AppMemberPO> pageData = appMemberRepository.page(buildBizParam(param));
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
		//2.是该应用的管理员角色
		PageData<AppMember> page = pageData(pageData);
		for(AppMember appUser : page.getItems()) {
			if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
				appUser.setModifyRights(YesOrNoEnum.YES.getCode());
				appUser.setDeleteRights(YesOrNoEnum.YES.getCode());
			}else if(appUser.getLoginName().equals(loginUser.getLoginName())){
				List<Integer> adminRole = Constants.ROLE_OF_OPERATE_APP_USER.stream()
						.filter(item -> appUser.getRoleTypes().contains(item))
						.collect(Collectors.toList());
				if(adminRole.size() > 0) {
					appUser.setModifyRights(YesOrNoEnum.YES.getCode());
					appUser.setDeleteRights(YesOrNoEnum.YES.getCode());
				}
			}
			//此外，当前登录用户不具有删除自己的权限
			if(loginUser.getLoginName().equals(appUser.getLoginName())) {
				appUser.setDeleteRights(YesOrNoEnum.NO.getCode());
			}
			appUser.setUserName(idName.get(appUser.getUserId()));
		}
		return page;
	}
	
	public Void addOrUpdate(LoginUser loginUser, AppMemberCreationParam addParam) {
		validateParam(addParam);
		//只有管理员才有操作权限
		hasAdminRights(loginUser, addParam.getAppId());
		if(CollectionUtils.isEmpty(addParam.getRoleTypes())){
			LogUtils.throwException(logger, MessageCodeEnum.ROLE_TYPE_IS_EMPTY);
		}
		//如果用户不存在，则进行保存
		SysUserPO sysUserPO = sysUserRepository.queryByLoginName(addParam.getLoginName());
		String userId = null;
		if(sysUserPO == null) {
			SysUserParam bizParam = new SysUserParam();
			bizParam.setLoginName(addParam.getLoginName());
			bizParam.setUserName(addParam.getLoginName());
			bizParam.setRegisteredSource(RegisteredSourceEnum.LDAP.getCode());
			bizParam.setRoleType(RoleTypeEnum.NORMAL.getCode());
			userId = sysUserRepository.add(bizParam);
		}else{
			userId = sysUserPO.getId();
		}
		//添加成员
		AppMemberParam bizParam = new AppMemberParam();
		bizParam.setAppId(addParam.getAppId());
		bizParam.setUserId(userId);
		AppMemberPO appMemberPO = appMemberRepository.query(bizParam);
		bizParam.setRoleTypes(addParam.getRoleTypes());
		if(appMemberPO != null) {
			bizParam.setId(appMemberPO.getId());
			appMemberRepository.update(bizParam);
		}else {
			bizParam.setLoginName(addParam.getLoginName());
			appMemberRepository.add(bizParam);
		}
		return null;
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Void delete(LoginUser loginUser, AppMemberDeletionParam deleteParam) {
		validateParam(deleteParam);
		//只有管理员才有操作权限
		hasAdminRights(loginUser, deleteParam.getAppId());
		//此外，当前登录用户不具有删除自己的权限
		if(loginUser.getLoginName().equals(deleteParam.getLoginName())) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		AppMemberParam bizParam = buildBizParam(deleteParam);
		AppMemberPO appMemberPO = appMemberRepository.query(bizParam);
		if(appMemberPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		appMemberRepository.delete(appMemberPO.getId());
		return null;
	}
	
	private void validateParam(AppMemberDeletionParam param) {
		if(StringUtils.isBlank(param.getLoginName())){
			LogUtils.throwException(logger, MessageCodeEnum.LOGIN_NAME_IS_EMPTY);
		}
		if(Objects.isNull(param.getAppId())){
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
	}
	
	private AppMemberParam buildBizParam(Serializable requestParam) {
		AppMemberParam bizParam = new AppMemberParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
	
	protected AppMember po2Dto(AppMemberPO po) {
		AppMember dto = super.po2Dto(po);
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
