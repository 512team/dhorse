package org.dhorse.rest.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.response.model.GlobalConfigAgg.CAS;
import org.dhorse.application.service.GlobalConfigApplicationService;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;

/**
 * CAS登录过滤器。<p/>
 * 因为需要查询数据库，所以需要在Spring容器启动完成以后，手动注册Bean对象。
 */
public class CasConfig implements BeanDefinitionRegistryPostProcessor{

	private static final List<String> URL_PATTERNS = Arrays.asList("/cas", "/page/sys_user/cas_login.html");
	
	@Autowired
	private GlobalConfigApplicationService globalConfigApplicationService;

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		BeanDefinitionBuilder bdf = BeanDefinitionBuilder
				.rootBeanDefinition(ServletListenerRegistrationBean.class)
				.addPropertyValue("listener", new SingleSignOutHttpSessionListener())
                .addPropertyValue("order", 1);
		
		CAS cas = globalConfigApplicationService.queryCas();
		BeanDefinitionBuilder bdf1 = BeanDefinitionBuilder
				.rootBeanDefinition(FilterRegistrationBean.class)
				.addPropertyValue("filter", new AuthenticationFilter())
                .addPropertyValue("urlPatterns", URL_PATTERNS)
                .addPropertyValue("order", 2);
		if (cas != null && YesOrNoEnum.YES.getCode().equals(cas.getEnable())) {
			Map<String, String> initParameters = new HashMap<String, String>();
			initParameters.put("casServerLoginUrl", cas.getServerLoginUrl());
			initParameters.put("serverName", cas.getClientHostUrl());
			bdf1.addPropertyValue("initParameters", initParameters);
		} else {
			bdf1.addPropertyValue("enabled", false);
		}
		
		BeanDefinitionBuilder bdf2 = BeanDefinitionBuilder
				.rootBeanDefinition(FilterRegistrationBean.class)
				.addPropertyValue("filter", new Cas30ProxyReceivingTicketValidationFilter())
                .addPropertyValue("urlPatterns", URL_PATTERNS)
                .addPropertyValue("order", 1);
		if (cas != null && YesOrNoEnum.YES.getCode().equals(cas.getEnable())) {
			Map<String, String> initParameters = new HashMap<String, String>();
			initParameters.put("casServerUrlPrefix", cas.getServerUrlPrefix());
			initParameters.put("serverName", cas.getClientHostUrl());
			bdf2.addPropertyValue("initParameters", initParameters);
		} else {
			bdf2.addPropertyValue("enabled", false);
		}
		
		registry.registerBeanDefinition("singleSignOutListenerRegistration", bdf.getBeanDefinition());
		registry.registerBeanDefinition("authFilterRegistration", bdf1.getBeanDefinition());
		registry.registerBeanDefinition("validFilterRegistration", bdf2.getBeanDefinition());
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
	}
}
