package org.dhorse.rest.component;

import org.dhorse.application.service.InitializingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationReadyListner implements ApplicationListener<ApplicationReadyEvent>{

	@Autowired
	private InitializingService initializingService;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		initializingService.asynInitConfig();
	}
}
