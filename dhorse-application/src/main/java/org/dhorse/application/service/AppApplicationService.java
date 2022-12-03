package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.LanguageTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.AppUserRoleTypeEnum;
import org.dhorse.api.param.app.AppCreationParam;
import org.dhorse.api.param.app.AppCreationParam.AppExtendJavaCreationParam;
import org.dhorse.api.param.app.AppDeletionParam;
import org.dhorse.api.param.app.AppPageParam;
import org.dhorse.api.param.app.AppUpdateParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.App;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.AppExtendJavaParam;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.AppParam;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppExtendJavaPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * 应用应用服务
 * 
 * @author 天地之怪
 */
@Service
public class AppApplicationService extends BaseApplicationService<App, AppPO> {

	private static final Logger logger = LoggerFactory.getLogger(AppApplicationService.class);
	
	public PageData<App> page(LoginUser loginUser, AppPageParam param) {
		return appRepository.page(loginUser, buildBizParam(param));
	}
	
	public App query(LoginUser loginUser, String appId) {
		return appRepository.queryWithExtendById(loginUser, appId);
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public App add(LoginUser loginUser, AppCreationParam addParam) {
		validateAddParam(addParam);
		AppParam param = new AppParam();
		param.setAppName(addParam.getAppName());
		if(appRepository.query(param) != null) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_NAME_EXISTENCE);
		}
		String porjectId = appRepository.add(buildBizParam(addParam));
		if(porjectId == null) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		AppExtendJavaCreationParam extendCreationParam = addParam.getExtendParam();
		if(extendCreationParam == null) {
			return null;
		}
		
		//添加扩展信息
		if(LanguageTypeEnum.JAVA.getCode().equals(addParam.getLanguageType())) {
			AppExtendJavaParam bizParam = new AppExtendJavaParam();
			bizParam.setAppId(porjectId);
			bizParam.setPackageBuildType(extendCreationParam.getPackageBuildType());
			bizParam.setPackageFileType(extendCreationParam.getPackageFileType());
			bizParam.setPackageTargetPath(extendCreationParam.getPackageTargetPath());
			if(appExtendJavaRepository.add(bizParam) == null) {
				LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
			}
		}
		
		//添加管理员
		AppMemberParam appMemberParam = new AppMemberParam();
		appMemberParam.setUserId(loginUser.getId());
		appMemberParam.setLoginName(loginUser.getLoginName());
		appMemberParam.setAppId(porjectId);
		appMemberParam.setRoleTypes(Arrays.asList(AppUserRoleTypeEnum.ADMIN.getCode()));
		appMemberRepository.add(appMemberParam);
		
		App app = new App();
		app.setId(porjectId);
		return app;
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Void update(LoginUser loginUser, AppUpdateParam updateParam) {
		if(StringUtils.isBlank(updateParam.getAppId())){
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
		validateAddParam(updateParam);
		AppParam appParam = buildBizParam(updateParam);
		appParam.setId(updateParam.getAppId());
		if(!appRepository.update(loginUser, appParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		//修改扩展信息
		AppExtendJavaParam bizParam = new AppExtendJavaParam();
		bizParam.setAppId(updateParam.getAppId());
		AppExtendJavaPO appExtendJavaPO = appExtendJavaRepository.query(bizParam);
		if(appExtendJavaPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_EXTEND_INEXISTENCE);
		}
		bizParam.setId(appExtendJavaPO.getId());
		bizParam.setPackageBuildType(updateParam.getExtendParam().getPackageBuildType());
		bizParam.setPackageFileType(updateParam.getExtendParam().getPackageFileType());
		bizParam.setPackageTargetPath(updateParam.getExtendParam().getPackageTargetPath());
		if(!appExtendJavaRepository.updateById(bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	@Transactional(rollbackFor = Throwable.class)
	public Void delete(LoginUser loginUser, AppDeletionParam deleteParam) {
		if(deleteParam.getAppId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
		//1.如果存在关联的环境，则不允许删除
		AppEnvParam appEnvParam = new AppEnvParam();
		appEnvParam.setAppId(deleteParam.getAppId());
		AppEnvPO appEnvPO = appEnvRepository.query(appEnvParam);
		if(appEnvPO != null) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ENV_DELETED);
		}
		//2.然后才能删除应用
		AppPO appPO = appRepository.queryById(deleteParam.getAppId());
		AppParam appParam = new AppParam();
		appParam.setId(deleteParam.getAppId());
		boolean isSuccessful = appRepository.delete(loginUser, appParam);
		if(LanguageTypeEnum.JAVA.getCode().equals(appPO.getLanguageType())){
			appExtendJavaRepository.deleteByAppId(deleteParam.getAppId());
		}
		appMemberRepository.deleteByAppId(deleteParam.getAppId());
		deploymentDetailRepository.deleteByAppId(deleteParam.getAppId());
		if(!isSuccessful) {
			LogUtils.throwException(logger, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	private void validateAddParam(AppCreationParam addParam) {
		if(StringUtils.isBlank(addParam.getAppName())){
			LogUtils.throwException(logger, MessageCodeEnum.APP_NAME_IS_EMPTY);
		}
		if(StringUtils.isBlank(addParam.getCodeRepoPath())){
			LogUtils.throwException(logger, MessageCodeEnum.CODE_REPO_PATH_IS_EMPTY);
		}
		if(Objects.isNull(addParam.getLanguageType())){
			LogUtils.throwException(logger, MessageCodeEnum.LANGUAGE_TYPE_IS_EMPTY);
		}
		if(addParam.getAppName().length() > 32) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "应用名称不能大于32个字符");
		}
		if(addParam.getBaseImage() != null && addParam.getBaseImage().length() > 128) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "依赖镜像不能大于128个字符");
		}
		if(addParam.getCodeRepoPath().length() > 64) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "代码仓库地址不能大于64个字符");
		}
		if(addParam.getFirstDepartment() != null && addParam.getFirstDepartment().length() > 16) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "一级部门不能大于16个字符");
		}
		if(addParam.getSecondDepartment() != null && addParam.getSecondDepartment().length() > 16) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "二级部门不能大于16个字符");
		}
		if(addParam.getThirdDepartment() != null && addParam.getThirdDepartment().length() > 16) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "三级部门不能大于16个字符");
		}
		if(addParam.getDescription() != null && addParam.getDescription().length() > 128) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "应用描述不能大于128个字符");
		}
	}
	
	private AppParam buildBizParam(Serializable requestParam) {
		AppParam bizParam = new AppParam();
		BeanUtils.copyProperties(requestParam, bizParam);
		return bizParam;
	}
}
