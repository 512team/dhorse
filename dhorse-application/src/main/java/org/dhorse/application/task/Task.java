package org.dhorse.application.task;

import org.dhorse.application.service.EnvReplicaApplicationService;
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
	
	@Scheduled(cron = "0/5 * * * * ?")
	public void collectPodMetrics() {
		try {
			replicaApplicationService.clearHistoryReplicaMetrics();
			replicaApplicationService.collectReplicaMetrics();
		}catch(Exception e) {
			logger.error("Failed to collect pod metrics", e);
		}
	}
}
