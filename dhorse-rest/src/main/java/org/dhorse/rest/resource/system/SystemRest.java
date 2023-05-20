package org.dhorse.rest.resource.system;

import org.dhorse.api.response.RestResponse;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.dhorse.rest.resource.AbstractRest;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemRest extends AbstractRest {
	
	@AccessNotLogin
	@GetMapping("/ping")
	public RestResponse<String> ping() {
		return this.success();
	}
	
	@AccessNotLogin
	@GetMapping("/shutdown")
	public RestResponse<String> shutdown() {
		ThreadPoolUtils.async(()->{
			int exitCode = SpringApplication.exit((ConfigurableApplicationContext)
					SpringBeanContext.getContext(), () -> 0);
		    System.exit(exitCode);
		});
		return this.success();
	}
}
