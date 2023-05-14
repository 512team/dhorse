package org.dhorse.application.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class InitializingService extends ApplicationService implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		//做一些事情
	}
}
