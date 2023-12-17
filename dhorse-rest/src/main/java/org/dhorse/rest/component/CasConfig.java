package org.dhorse.rest.component;

import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.response.model.GlobalConfigAgg.CAS;
import org.dhorse.application.service.GlobalConfigApplicationService;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.validation.AbstractTicketValidationFilter;
import org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CAS登录过滤器
 */
@Configuration
public class CasConfig {

	private static final Logger logger = LoggerFactory.getLogger(CasConfig.class);
	
	private static final List<String> URL_PATTERNS = Arrays.asList("/cas", "/page/sys_user/cas_login.html");
	
	@Autowired
	private GlobalConfigApplicationService globalConfigApplicationService;

	@Bean
	public FilterRegistrationBean<AuthenticationFilter> authFilterRegistration() {
		CAS cas = null;
		try {
			cas = globalConfigApplicationService.queryCas();
		}catch(Exception e) {
			//如果读取配置失败，不阻塞启动
			logger.warn("Failed to query cas config");
		}
		FilterRegistrationBean<AuthenticationFilter> fr = new FilterRegistrationBean<>();
		Map<String, String> initParameters = new HashMap<String, String>();
		if (cas != null && YesOrNoEnum.YES.getCode().equals(cas.getEnable())) {
			initParameters.put("casServerLoginUrl", cas.getServerLoginUrl());
			initParameters.put("serverName", cas.getClientHostUrl());
		} else {
			fr.setEnabled(false);
		}
		fr.setFilter(new AuthenticationFilter());
		fr.setInitParameters(initParameters);
		fr.setUrlPatterns(URL_PATTERNS);
		fr.setOrder(2);
		return fr;
	}

	@Bean
	public FilterRegistrationBean<AbstractTicketValidationFilter> validFilterRegistration() {
		CAS cas = null;
		try {
			cas = globalConfigApplicationService.queryCas();
		}catch(Exception e) {
			//如果读取配置失败，不阻塞启动
			logger.warn("Failed to query cas config");
		}
		FilterRegistrationBean<AbstractTicketValidationFilter> fr = new FilterRegistrationBean<>();
		Map<String, String> initParameters = new HashMap<String, String>();
		if (cas != null && YesOrNoEnum.YES.getCode().equals(cas.getEnable())) {
			initParameters.put("casServerUrlPrefix", cas.getServerUrlPrefix());
			initParameters.put("serverName", cas.getClientHostUrl());
		} else {
			fr.setEnabled(false);
		}
		fr.setFilter(new Cas30ProxyReceivingTicketValidationFilter());
		fr.setInitParameters(initParameters);
		fr.setUrlPatterns(URL_PATTERNS);
		fr.setOrder(1);
		return fr;
	}

	@Bean
	public ServletListenerRegistrationBean<EventListener> singleSignOutListenerRegistration() {
		ServletListenerRegistrationBean<EventListener> registrationBean
			= new ServletListenerRegistrationBean<EventListener>();
		registrationBean.setListener(new SingleSignOutHttpSessionListener());
		registrationBean.setOrder(1);
		return registrationBean;
	}
}
