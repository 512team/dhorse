package org.dhorse.rest.component;

import org.dhorse.application.service.ClusterApplicationService;
import org.dhorse.rest.websocket.WebSocketCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationClosedListner implements ApplicationListener<ContextClosedEvent>{
	
	@Autowired
	private ClusterApplicationService clusterApplicationService;
	
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		clusterApplicationService.deleteDHorseConfig();
		WebSocketCache.removeAllReplicaLog();
	}
}
