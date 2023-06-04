package org.dhorse.rest.resource;

import org.dhorse.api.param.app.env.EnvHealthQueryParam;
import org.dhorse.api.param.app.env.EnvLifeCycleQueryParam;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.response.model.EnvHealth;
import org.dhorse.api.response.model.EnvLifecycle;
import org.dhorse.application.service.EnvExtApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 环境扩展服务
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/env/ext")
public class EnvExtRest extends AbstractRest {

	@Autowired
	private EnvExtApplicationService envExtApplicationService;

	/**
	 * 查询环境健康配置
	 * 
	 * @return 符合条件的数据
	 */
	@PostMapping("/queryEnvHealth")
	public RestResponse<EnvHealth> queryEnvHealth(@CookieValue("login_token") String loginToken,
			@RequestBody EnvHealthQueryParam queryParam) {
		return success(envExtApplicationService.queryEnvHealth(queryLoginUserByToken(loginToken), queryParam));
	}

	/**
	 * 添加（修改）境健康配置
	 * 
	 * @param envHealthParam 添加（修改）参数
	 * @return 无
	 */
	@PostMapping("/addOrUpdateEnvHealth")
	public RestResponse<Void> addOrUpdateEnvHealth(@CookieValue("login_token") String loginToken,
			@RequestBody EnvHealth envHealthParam) {
		return success(envExtApplicationService.addOrUpdateEnvHealth(queryLoginUserByToken(loginToken), envHealthParam));
	}
	
	/**
	 * 查询生命周期配置
	 * 
	 * @return 符合条件的数据
	 */
	@PostMapping("/queryLifecycle")
	public RestResponse<EnvLifecycle> queryLifecycle(@CookieValue("login_token") String loginToken,
			@RequestBody EnvLifeCycleQueryParam queryParam) {
		return success(envExtApplicationService.queryLifecycle(queryLoginUserByToken(loginToken), queryParam));
	}

	/**
	 * 添加（修改）环境周期配置
	 * 
	 * @param addParam 添加（修改）参数
	 * @return 无
	 */
	@PostMapping("/addOrUpdateLifecycle")
	public RestResponse<Void> addOrUpdateLifecycle(@CookieValue("login_token") String loginToken,
			@RequestBody EnvLifecycle addParam) {
		return success(envExtApplicationService.addOrUpdateLifecycle(queryLoginUserByToken(loginToken), addParam));
	}
}