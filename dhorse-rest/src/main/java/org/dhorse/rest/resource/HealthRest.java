package org.dhorse.rest.resource;

import org.dhorse.api.response.RestResponse;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthRest extends AbstractRest {
	
	@AccessNotLogin
	@GetMapping("/ping")
	public RestResponse<String> ping() {
		return this.success();
	}
}
