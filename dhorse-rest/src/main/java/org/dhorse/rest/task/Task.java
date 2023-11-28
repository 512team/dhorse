package org.dhorse.rest.task;

import java.util.Date;

import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.application.service.InitializingService;
import org.dhorse.infrastructure.repository.LogRecordRepository;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DateUtils;
import org.dhorse.rest.websocket.WebSocketCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Task {
	
	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	
	@Autowired
	private EnvReplicaApplicationService replicaApplicationService;
	
	@Autowired
	private LogRecordRepository logRecordRepository;
	
	@Autowired
	private InitializingService initializingService;
	
	/**
	 * 10s定时任务
	 */
	@Scheduled(cron = "0/10 * * * * ?")
	public void collectReplicaMetrics() {
		//收集副本的指标数据
		try {
			replicaApplicationService.collectReplicaMetrics();
		}catch(Exception e) {
			logger.error("Failed to collect pod metrics", e);
		}
		
		//上报服务IP地址
		try {
			initializingService.reportServerIp();
		}catch(Exception e) {
			logger.error("Failed to report server ip", e);
		}
	}
	
	/**
	 * 清除历史数据，每天0点执行
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void clearHistoryDB() {
		Date now = new Date();
		try {
			//清除指标数据
			replicaApplicationService.clearMetrics(DateUtils.addDays(now, -Constants.DAYS_3));
			//清除日志
			logRecordRepository.delete(DateUtils.addDays(now, -Constants.DAYS_14));
		}catch(Exception e) {
			logger.error("Failed to clear db", e);
		}
	}
	
	/**
	 * 清除过期的WebSocket，每分钟执行一次
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void clearSocket() {
		try {
			WebSocketCache.removeExpired();
		}catch(Exception e) {
			logger.error("Failed to clear websocket", e);
		}
	}
}
