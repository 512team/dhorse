package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.LanguageTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.ProjectUserRoleTypeEnum;
import org.dhorse.api.param.project.ProjectCreationParam;
import org.dhorse.api.param.project.ProjectCreationParam.ProjectExtendJavaCreationParam;
import org.dhorse.api.param.project.ProjectDeletionParam;
import org.dhorse.api.param.project.ProjectPageParam;
import org.dhorse.api.param.project.ProjectUpdateParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.Project;
import org.dhorse.infrastructure.param.ProjectEnvParam;
import org.dhorse.infrastructure.param.ProjectExtendJavaParam;
import org.dhorse.infrastructure.param.ProjectMemberParam;
import org.dhorse.infrastructure.param.ProjectParam;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.repository.po.ProjectExtendJavaPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * 项目应用服务
 * 
 * @author 天地之怪
 */
@Service
public class ProjectApplicationService extends BaseApplicationService<Project, ProjectPO> {

	private static final Logger logger = LoggerFactory.getLogger(ProjectApplicationService.class);
	
	public PageData<Project> page(LoginUser loginUser, ProjectPageParam param) {
		return projectRepository.page(loginUser, buildBizParam(param));
	}
	
	public Project query(LoginUser loginUser, String projectId) {
		return projectRepository.queryWithExtendById(loginUser, projectId);
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Project add(LoginUser loginUser, ProjectCreationParam addParam) {
		validateAddParam(addParam);
		ProjectParam param = new ProjectParam();
		param.setProjectName(addParam.getProjectName());
		if(projectRepository.query(param) != null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_NAME_EXISTENCE);
		}
		String porjectId = projectRepository.add(buildBizParam(addParam));
		if(porjectId == null) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		ProjectExtendJavaCreationParam extendCreationParam = addParam.getExtendParam();
		if(extendCreationParam == null) {
			return null;
		}
		
		//添加扩展信息
		if(LanguageTypeEnum.JAVA.getCode().equals(addParam.getLanguageType())) {
			ProjectExtendJavaParam bizParam = new ProjectExtendJavaParam();
			bizParam.setProjectId(porjectId);
			bizParam.setPackageBuildType(extendCreationParam.getPackageBuildType());
			bizParam.setPackageFileType(extendCreationParam.getPackageFileType());
			bizParam.setPackageTargetPath(extendCreationParam.getPackageTargetPath());
			if(projectExtendJavaRepository.add(bizParam) == null) {
				LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
			}
		}
		
		//添加管理员
		ProjectMemberParam projectMemberParam = new ProjectMemberParam();
		projectMemberParam.setUserId(loginUser.getId());
		projectMemberParam.setLoginName(loginUser.getLoginName());
		projectMemberParam.setProjectId(porjectId);
		projectMemberParam.setRoleTypes(Arrays.asList(ProjectUserRoleTypeEnum.ADMIN.getCode()));
		projectMemberRepository.add(projectMemberParam);
		
		Project project = new Project();
		project.setId(porjectId);
		return project;
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Void update(LoginUser loginUser, ProjectUpdateParam updateParam) {
		if(StringUtils.isBlank(updateParam.getProjectId())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		validateAddParam(updateParam);
		ProjectParam projectParam = buildBizParam(updateParam);
		projectParam.setId(updateParam.getProjectId());
		if(!projectRepository.update(loginUser, projectParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		//修改扩展信息
		ProjectExtendJavaParam bizParam = new ProjectExtendJavaParam();
		bizParam.setProjectId(updateParam.getProjectId());
		ProjectExtendJavaPO projectExtendJavaPO = projectExtendJavaRepository.query(bizParam);
		if(projectExtendJavaPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_EXTEND_INEXISTENCE);
		}
		bizParam.setId(projectExtendJavaPO.getId());
		bizParam.setPackageBuildType(updateParam.getExtendParam().getPackageBuildType());
		bizParam.setPackageFileType(updateParam.getExtendParam().getPackageFileType());
		bizParam.setPackageTargetPath(updateParam.getExtendParam().getPackageTargetPath());
		if(!projectExtendJavaRepository.updateById(bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Void delete(LoginUser loginUser, ProjectDeletionParam deleteParam) {
		if(deleteParam.getProjectId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ID_IS_NULL);
		}
		//1.如果存在关联的环境，则不允许删除
		ProjectEnvParam projectEnvParam = new ProjectEnvParam();
		projectEnvParam.setProjectId(deleteParam.getProjectId());
		ProjectEnvPO projectEnvPO = projectEnvRepository.query(projectEnvParam);
		if(projectEnvPO != null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_ENV_DELETED);
		}
		//2.然后才能删除项目
		ProjectPO projectPO = projectRepository.queryById(deleteParam.getProjectId());
		ProjectParam projectParam = new ProjectParam();
		projectParam.setId(deleteParam.getProjectId());
		boolean isSuccessful = projectRepository.delete(loginUser, projectParam);
		if(LanguageTypeEnum.JAVA.getCode().equals(projectPO.getLanguageType())){
			projectExtendJavaRepository.deleteByProjectId(deleteParam.getProjectId());
		}
		projectMemberRepository.deleteByProjectId(deleteParam.getProjectId());
		deploymentDetailRepository.deleteByProjectId(deleteParam.getProjectId());
		if(!isSuccessful) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	private void validateAddParam(ProjectCreationParam addParam) {
		if(StringUtils.isBlank(addParam.getProjectName())){
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_NAME_IS_EMPTY);
		}
		if(StringUtils.isBlank(addParam.getCodeRepoPath())){
			LogUtils.throwException(logger, MessageCodeEnum.CODE_REPO_PATH_IS_EMPTY);
		}
		if(Objects.isNull(addParam.getLanguageType())){
			LogUtils.throwException(logger, MessageCodeEnum.LANGUAGE_TYPE_IS_EMPTY);
		}
	}
	
	private ProjectParam buildBizParam(Serializable requestParam) {
		ProjectParam bizParam = new ProjectParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
}
