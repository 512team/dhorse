package org.dhorse.rest.resource;

import org.dhorse.api.event.DeploymentMessage;
import org.dhorse.api.response.EventResponse;
import org.dhorse.api.response.RestResponse;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AccessNotLogin
@RestController
@RequestMapping("/event")
public class EventRest extends AbstractRest {
	
	@PostMapping("/receive")
	public RestResponse<Void> receive(@RequestBody EventResponse<DeploymentMessage> reponse) {
		return success();
	}
}
