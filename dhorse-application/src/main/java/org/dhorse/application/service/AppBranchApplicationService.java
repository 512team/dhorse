package org.dhorse.application.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.app.branch.AppBranchCreationParam;
import org.dhorse.api.param.app.branch.AppBranchDeletionParam;
import org.dhorse.api.param.app.branch.AppBranchListParam;
import org.dhorse.api.param.app.branch.AppBranchPageParam;
import org.dhorse.api.param.app.branch.BuildParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppBranch;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.strategy.repo.param.BranchListParam;
import org.dhorse.infrastructure.strategy.repo.param.BranchPageParam;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 应用分支服务
 * 
 * @author 天地之怪
 */
@Service
public class AppBranchApplicationService extends DeployApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(AppBranchApplicationService.class);

	public PageData<AppBranch> page(LoginUser loginUser, AppBranchPageParam pageParam) {
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			AppMemberPO appMember = appMemberRepository
					.queryByLoginNameAndAppId(loginUser.getLoginName(), pageParam.getAppId());
			if (appMember == null) {
				return zeroPageData(pageParam.getPageSize());
			}
		}
		AppPO appPO = validateApp(pageParam.getAppId());
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		if(globalConfigAgg.getCodeRepo() == null) {
			return zeroPageData(pageParam.getPageSize());
		}
		BranchPageParam branchPageParam = new BranchPageParam();
		branchPageParam.setPageNum(pageParam.getPageNum());
		branchPageParam.setPageSize(pageParam.getPageSize());
		branchPageParam.setAppIdOrPath(appPO.getCodeRepoPath());
		branchPageParam.setBranchName(pageParam.getBranchName());
		PageData<AppBranch> pageData = buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
				.branchPage(globalConfigAgg.getCodeRepo(), branchPageParam);
		return pageData;
	}

	public List<AppBranch> list(LoginUser loginUser, AppBranchListParam listParam) {
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			AppMemberPO appMember = appMemberRepository
					.queryByLoginNameAndAppId(loginUser.getLoginName(), listParam.getAppId());
			if (appMember == null) {
				return Collections.emptyList();
			}
		}
		AppPO appPO = validateApp(listParam.getAppId());
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		BranchListParam branchListParam = new BranchListParam();
		branchListParam.setAppIdOrPath(appPO.getCodeRepoPath());
		branchListParam.setBranchName(listParam.getBranchName());
		return buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
				.branchList(globalConfigAgg.getCodeRepo(), branchListParam);
	}
	
	public Void add(LoginUser loginUser, AppBranchCreationParam addParam) {
		validateAddParam(addParam);
		hasRights(loginUser, addParam.getAppId());
		AppPO appPO = validateApp(addParam.getAppId());
		// 创建仓库分支
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
			.createBranch(globalConfigAgg.getCodeRepo(),
				appPO.getCodeRepoPath(), addParam.getBranchName(), addParam.getOrgBranchName());
		return null;
	}

	public Void delete(LoginUser loginUser, AppBranchDeletionParam deleteParam) {
		validateAddParam(deleteParam);
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			AppMemberPO appMember = appMemberRepository
					.queryByLoginNameAndAppId(loginUser.getLoginName(), deleteParam.getAppId());
			if (appMember == null) {
				LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
			}
		}
		AppPO appPO = validateApp(deleteParam.getAppId());
		// 创建仓库分支
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
			.deleteBranch(globalConfigAgg.getCodeRepo(),
				appPO.getCodeRepoPath(), deleteParam.getBranchName());
		return null;
	}

	private void validateAddParam(AppBranchCreationParam addParam) {
		if (StringUtils.isBlank(addParam.getAppId())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
		if (StringUtils.isBlank(addParam.getBranchName())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_BRANCH_NAME_IS_EMPTY);
		}
	}

	public String buildVersion(LoginUser loginUser, BuildParam buildParam) {
		if (StringUtils.isBlank(buildParam.getAppId())) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
		hasRights(loginUser, buildParam.getAppId());
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		if (globalConfigRepository.count(globalConfigParam) < 1) {
			LogUtils.throwException(logger, MessageCodeEnum.INIT_GLOBAL_CONFIG);
		}
		buildParam.setSubmitter(loginUser.getLoginName());
		return buildVersion(buildParam);
	}
}