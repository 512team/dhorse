package org.dhorse.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.GlobalConfigPO;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.HttpUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class InitializingService extends ApplicationService implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(InitializingService.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		//做一些事情
	}
	
	/**
	 * 上报DHorse服务器地址。<p/>
	 * 在集群部署的情况下，存在多服务器同时上报的情况，固采用乐观锁方式进行更新，
	 * 重试5次后失败，则在下个阶段上报。
	 */
	public void reportServerIp() {
		
		//随机休眠，减小集群任务的并发性
		randomSleep();
				
		String currentIp = Constants.hostIp() + ":" + componentConstants.getServerPort();
		for(int i = 0; i < 5; i++) {
			if(doReport(currentIp)) {
				break;
			}
		}
	}
	
	private boolean doReport(String currentIp) {
		GlobalConfigParam param = new GlobalConfigParam();
		param.setItemType(GlobalConfigItemTypeEnum.SERVER_IP.getCode());
		GlobalConfigPO po = globalConfigRepository.query(param);
		
		Set<String> ipSet = new HashSet<>();
		ipSet.add(currentIp);
		
		if(po == null) {
			param.setItemValue(JsonUtils.toJsonString(ipSet));
			globalConfigRepository.add(param);
			return true;
		}
		
		if(!StringUtils.isEmpty(po.getItemValue())) {
			List<String> ips = JsonUtils.parseToList(po.getItemValue(), String.class);
			for(String ip : ips) {
				if(HttpUtils.pingDHorseServer(ip)) {
					ipSet.add(ip);
				}
			}
		}
		
		param.setId(po.getId());
		param.setItemValue(JsonUtils.toJsonString(ipSet));
		param.setVersion(po.getVersion());
		return globalConfigRepository.updateById(param);
	}
	
	@Bean
	public Scheduler scheduler() {
		try {
			return new StdSchedulerFactory().getScheduler();
		} catch (SchedulerException e) {
			logger.error("Failed to create bean [Scheduler]", e);
		}
		return null;
	}
}
