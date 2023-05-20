package org.dhorse.infrastructure.component;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanContext implements ApplicationContextAware{

	private static ApplicationContext APPLICATION_CONTEXT;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		APPLICATION_CONTEXT = applicationContext;
	}
	
	public static <T> T getBean(Class<T> clazz){
		return APPLICATION_CONTEXT.getBean(clazz);
	}
	
	public static ApplicationContext getContext(){
		return APPLICATION_CONTEXT;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
