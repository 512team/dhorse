package org.dhorse.rest.resource;

import org.dhorse.api.result.RestResponse;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRest {

	@Autowired
	protected SysUserApplicationService sysUserApplicationService;
	
	protected LoginUser queryLoginUserByToken(String loginToken) {
		return sysUserApplicationService.queryLoginUserByToken(loginToken);
	}

	protected <D> RestResponse<D> success() {
		return new RestResponse<D>();
	}
	
	protected <D> RestResponse<D> success(D data) {
		return new RestResponse<D>(data);
	}

	protected <D> RestResponse<D> error(ApplicationException e) {
		return new RestResponse<D>(e.getCode(), e.getMessage());
	}
	
	
}
