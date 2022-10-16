package org.dhorse.rest.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 
 * 配置拦截器
 * 
 * @author Dahai
 */
@Configuration
public class DefaultWebMvcConfigurer implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor()).addPathPatterns("/**").excludePathPatterns("/error", "/**/*.css",
				"/**.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif", "/**/*.ico");
	}

	@Bean
	public HandlerInterceptor authInterceptor() {
		return new AuthInterceptor();
	}

}
