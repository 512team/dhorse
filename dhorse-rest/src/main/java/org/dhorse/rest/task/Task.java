package org.dhorse.rest.task;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.infrastructure.repository.LogRecordRepository;
import org.dhorse.infrastructure.utils.Constants;
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
	
	/**
	 * 收集副本的指标数据
	 */
	@Scheduled(cron = "0/5 * * * * ?")
	public void collectReplicaMetrics() {
		try {
			replicaApplicationService.collectReplicaMetrics();
		}catch(Exception e) {
			logger.error("Failed to collect pod metrics", e);
		}
	}
	
	/**
	 * 清除历史数据
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void clearHistoryDB() {
		Date now = new Date();
		try {
			//清除副本指标
			replicaApplicationService.clearHistoryReplicaMetrics(DateUtils.addDays(now, -3));
			//清除日志
			logRecordRepository.deleteBefore(DateUtils.addDays(now, -Constants.DEPLOYED_LOG_EXIST_DAYS));
		}catch(Exception e) {
			logger.error("Failed to clear db", e);
		}
	}
	
	/**
	 * 清除过期的WebSocket
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
