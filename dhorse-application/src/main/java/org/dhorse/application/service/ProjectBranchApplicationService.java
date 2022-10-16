package org.dhorse.application.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.project.branch.ProjectBranchCreationParam;
import org.dhorse.api.param.project.branch.ProjectBranchDeletionParam;
import org.dhorse.api.param.project.branch.ProjectBranchListParam;
import org.dhorse.api.param.project.branch.ProjectBranchPageParam;
import org.dhorse.api.param.project.branch.VersionBuildParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.ProjectBranch;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.strategy.repo.param.BranchListParam;
import org.dhorse.infrastructure.strategy.repo.param.BranchPageParam;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * 项目分支应用服务
 * 
 * @author 天地之怪
 */
@Service
public class ProjectBranchApplicationService extends DeployApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectBranchApplicationService.class);

	public PageData<ProjectBranch> page(LoginUser loginUser, ProjectBranchPageParam pageParam) {
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			ProjectMemberPO projectMember = projectMemberRepository
					.queryByLoginNameAndProjectId(loginUser.getLoginName(), pageParam.getProjectId());
			if (projectMember == null) {
				return zeroPageData(pageParam.getPageSize());
			}
		}
		ProjectPO projectPO = validateProject(pageParam.getProjectId());
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		BranchPageParam branchPageParam = new BranchPageParam();
		branchPageParam.setPageNum(pageParam.getPageNum());
		branchPageParam.setPageSize(pageParam.getPageSize());
		branchPageParam.setProjectIdOrPath(projectPO.getCodeRepoPath());
		branchPageParam.setBranchName(pageParam.getBranchName());
		PageData<ProjectBranch> pageData = buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
				.branchPage(globalConfigAgg.getCodeRepo(), branchPageParam);
		return pageData;
	}

	public List<ProjectBranch> list(LoginUser loginUser, ProjectBranchListParam listParam) {
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			ProjectMemberPO projectMember = projectMemberRepository
					.queryByLoginNameAndProjectId(loginUser.getLoginName(), listParam.getProjectId());
			if (projectMember == null) {
				return Collections.emptyList();
			}
		}
		ProjectPO projectPO = validateProject(listParam.getProjectId());
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		BranchListParam branchListParam = new BranchListParam();
		branchListParam.setProjectIdOrPath(projectPO.getCodeRepoPath());
		branchListParam.setBranchName(listParam.getBranchName());
		return buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
				.branchList(globalConfigAgg.getCodeRepo(), branchListParam);
	}
	
	public Void add(LoginUser loginUser, ProjectBranchCreationParam addParam) {
		validateAddParam(addParam);
		hasRights(loginUser, addParam.getProjectId());
		ProjectPO projectPO = validateProject(addParam.getProjectId());
		// 创建仓库分支
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
			.createBranch(globalConfigAgg.getCodeRepo(),
				projectPO.getCodeRepoPath(), addParam.getBranchName());
		return null;
	}

	public Void delete(LoginUser loginUser, ProjectBranchDeletionParam deleteParam) {
		validateAddParam(deleteParam);
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			ProjectMemberPO projectMember = projectMemberRepository
					.queryByLoginNameAndProjectId(loginUser.getLoginName(), deleteParam.getProjectId());
			if (projectMember == null) {
				LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
			}
		}
		ProjectPO projectPO = validateProject(deleteParam.getProjectId());
		// 创建仓库分支
		GlobalConfigAgg globalConfigAgg = this.globalConfig();
		buildCodeRepo(globalConfigAgg.getCodeRepo().getType())
			.deleteBranch(globalConfigAgg.getCodeRepo(),
				projectPO.getCodeRepoPath(), deleteParam.getBranchName());
		return null;
	}

	private void validateAddParam(ProjectBranchCreationParam addParam) {
		if (StringUtils.isBlank(addParam.getProjectId())) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		if (StringUtils.isBlank(addParam.getBranchName())) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_ID_IS_EMPTY);
		}
	}

	public String buildVersion(LoginUser loginUser, VersionBuildParam versionBuildParam) {
		if (StringUtils.isBlank(versionBuildParam.getProjectId())) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		hasRights(loginUser, versionBuildParam.getProjectId());
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		if (globalConfigRepository.count(globalConfigParam) < 1) {
			LogUtils.throwException(logger, MessageCodeEnum.INIT_GLOBAL_CONFIG);
		}
		return buildVersion(versionBuildParam);
	}
}