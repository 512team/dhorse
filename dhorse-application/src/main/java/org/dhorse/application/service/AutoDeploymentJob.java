package org.dhorse.application.service;

import org.dhorse.api.enums.CodeTypeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.app.branch.BuildParam;
import org.dhorse.api.param.app.branch.deploy.DeploymentParam;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.Constants;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自动部署任务
 */
public class AutoDeploymentJob implements Job {
 
	private static final Logger logger = LoggerFactory.getLogger(AutoDeploymentJob.class);
	
    @Override
    public void execute(JobExecutionContext context) {
    	JobDataMap dataMap = context.getMergedJobDataMap();
    	int codeType = dataMap.getInt("codeType");
    	BuildParam buildParam = new BuildParam();
    	buildParam.setAppId(dataMap.getString("appId"));
    	buildParam.setEnvId(dataMap.getString("envId"));
    	buildParam.setBranchName(dataMap.getString("branchName"));
    	buildParam.setSubmitter(Constants.DHORSE_TAG);
    	try {
    		String versionName = "";
	    	if(codeType == CodeTypeEnum.BRANCH.getCode()) {
	    		AppBranchApplicationService appBranchApplicationService = SpringBeanContext.getBean(AppBranchApplicationService.class);
	    		versionName = appBranchApplicationService.buildVersion(buildParam);
	    	}else if(codeType == CodeTypeEnum.TAG.getCode()){
	    		AppTagApplicationService appTagApplicationService = SpringBeanContext.getBean(AppTagApplicationService.class);
	    		versionName = appTagApplicationService.buildVersion(buildParam);
	    	}
	    	DeploymentVersionApplicationService deploymentVersionApplicationService = SpringBeanContext.getBean(DeploymentVersionApplicationService.class);
	    	LoginUser loginUser = new LoginUser();
	    	loginUser.setLoginName("admin");
	    	loginUser.setRoleType(RoleTypeEnum.ADMIN.getCode());
	    	DeploymentParam deploymentParam = new DeploymentParam();
	    	deploymentParam.setAppId(buildParam.getAppId());
	    	deploymentParam.setEnvId(buildParam.getEnvId());
	    	deploymentParam.setVersionName(versionName);
	    	deploymentVersionApplicationService.submitToDeploy(loginUser, deploymentParam);
    	}catch(Exception e) {
    		logger.error("Failed to start auto deployment, envId: " + buildParam.getEnvId(), e);
    	}
    }
}