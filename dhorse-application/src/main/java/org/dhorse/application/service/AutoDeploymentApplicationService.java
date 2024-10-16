package org.dhorse.application.service;

import javax.annotation.PreDestroy;

import org.dhorse.api.enums.EnvExtTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.response.model.EnvAutoDeployment;
import org.dhorse.infrastructure.param.EnvExtParam;
import org.dhorse.infrastructure.repository.po.EnvExtPO;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 自动部署环境服务
 *
 * @author 天地之怪
 */
@Service
public class AutoDeploymentApplicationService extends ApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(AutoDeploymentApplicationService.class);
	
	private static final String JOB_NAME_PRE_KEY = "auto-deployment-";

	@Autowired
	private Scheduler scheduler;
	
	public void startAllJob() {
		ThreadPoolUtils.AUTO_DEPLOYMENT_THREAD.submit(() -> doStartAllJob());
	}
	
	private void doStartAllJob() {
		logger.info("Start to automatically deploy");
		EnvExtParam extParam = new EnvExtParam();
		extParam.setExType(EnvExtTypeEnum.AUTO_DEPLOYMENT.getCode());
		extParam.setPageNum(1);
		extParam.setPageSize(100);
		IPage<EnvExtPO> page = envExtRepository.page(extParam);
		for(int i = 1; i <= page.getPages(); i++) {
			extParam.setPageNum(i);
			page = envExtRepository.page(extParam);
			for(EnvExtPO po : page.getRecords()) {
				EnvAutoDeployment ad = JsonUtils.parseToObject(po.getExt(), EnvAutoDeployment.class);
				ad.setAppId(po.getAppId());
				ad.setEnvId(po.getEnvId());
				updateJob(ad);
			}
		}
        try {
        	scheduler.start();
        } catch (SchedulerException e) {
        	logger.error("Failed to start auto deployment job", e);
        }
	}
	
	public void updateJob(EnvAutoDeployment updateParam) {
		JobKey jobKey = JobKey.jobKey(JOB_NAME_PRE_KEY + updateParam.getEnvId());
		boolean exists = false;
		try {
			exists = scheduler.checkExists(jobKey);
		} catch (SchedulerException e) {
			logger.error("Failed to check auto deployment job", e);
		}
		if(updateParam.getEnable() == null || !updateParam.getEnable().equals(YesOrNoEnum.YES.getCode())) {
			try {
				if(exists) {
					scheduler.deleteJob(jobKey);
				}
			} catch (SchedulerException e) {
				logger.error("Failed to delete auto deployment job", e);
			}
			return;
		}
		JobDetail job = JobBuilder.newJob(AutoDeploymentJob.class)
                .withIdentity(JOB_NAME_PRE_KEY + updateParam.getEnvId())
                .build();
		JobDataMap dataMap = new JobDataMap();
		dataMap.put("appId", updateParam.getAppId());
		dataMap.put("envId", updateParam.getEnvId());
		dataMap.put("codeType", updateParam.getCodeType());
		dataMap.put("branchName", updateParam.getBranchName());
		CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(JOB_NAME_PRE_KEY + updateParam.getEnvId())
                .withSchedule(CronScheduleBuilder.cronSchedule(updateParam.getCron()))
                .usingJobData(dataMap)
                .build();
		try {
			if(exists) {
				scheduler.rescheduleJob(TriggerKey.triggerKey(JOB_NAME_PRE_KEY + updateParam.getEnvId()), trigger);
			}else {
				scheduler.scheduleJob(job, trigger);
			}
		} catch (SchedulerException e) {
        	logger.error("Failed to add auto deployment job", e);
        }
	}
	
	@PreDestroy
	public void destroy() {
		try {
			if (!scheduler.isShutdown()) {
				scheduler.shutdown(true);
			}
		} catch (SchedulerException e) {
			logger.error("Failed to destroy bean [Scheduler]", e);
		}
	}
}