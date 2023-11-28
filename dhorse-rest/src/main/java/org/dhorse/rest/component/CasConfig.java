package org.dhorse.rest.component;

import java.util.EventListener;

import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CAS登录过滤器
 */
@Configuration
public class CasConfig {

//	private static final List<String> URL_PATTERNS = Arrays.asList("/cas", "/page/sys_user/cas_login.html");
//	
//	@Autowired
//	private GlobalConfigApplicationService globalConfigApplicationService;
//
//	@Bean
//	public FilterRegistrationBean<AuthenticationFilter> authFilterRegistration() {
//		CAS cas = globalConfigApplicationService.queryCas();
//		FilterRegistrationBean<AuthenticationFilter> fr = new FilterRegistrationBean<>();
//		Map<String, String> initParameters = new HashMap<String, String>();
//		if (cas != null && YesOrNoEnum.YES.getCode().equals(cas.getEnable())) {
//			initParameters.put("casServerLoginUrl", cas.getServerLoginUrl());
//			initParameters.put("serverName", cas.getClientHostUrl());
//		} else {
//			fr.setEnabled(false);
//		}
//		fr.setFilter(new AuthenticationFilter());
//		fr.setInitParameters(initParameters);
//		fr.setUrlPatterns(URL_PATTERNS);
//		fr.setOrder(2);
//		return fr;
//	}
//
//	@Bean
//	public FilterRegistrationBean<AbstractTicketValidationFilter> validFilterRegistration() {
//		CAS cas = globalConfigApplicationService.queryCas();
//		FilterRegistrationBean<AbstractTicketValidationFilter> fr = new FilterRegistrationBean<>();
//		Map<String, String> initParameters = new HashMap<String, String>();
//		if (cas != null && YesOrNoEnum.YES.getCode().equals(cas.getEnable())) {
//			initParameters.put("casServerUrlPrefix", cas.getServerUrlPrefix());
//			initParameters.put("serverName", cas.getClientHostUrl());
//		} else {
//			fr.setEnabled(false);
//		}
//		fr.setFilter(new Cas30ProxyReceivingTicketValidationFilter());
//		fr.setInitParameters(initParameters);
//		fr.setUrlPatterns(URL_PATTERNS);
//		fr.setOrder(1);
//		return fr;
//	}

	@Bean
	public ServletListenerRegistrationBean<EventListener> singleSignOutListenerRegistration() {
		ServletListenerRegistrationBean<EventListener> registrationBean
			= new ServletListenerRegistrationBean<EventListener>();
		registrationBean.setListener(new SingleSignOutHttpSessionListener());
		registrationBean.setOrder(1);
		return registrationBean;
	}
}
