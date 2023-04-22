package org.dhorse.rest.resource;

import org.dhorse.api.response.RestResponse;
import org.dhorse.api.vo.EnvHealth;
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
@RequestMapping("/app/env/ext")
public class AppExtRest extends AbstractRest {

	@Autowired
	private EnvExtApplicationService envExtApplicationService;

	/**
	 * 查询环境健康配置
	 * 
	 * @return 符合条件的数据
	 */
	@PostMapping("/queryEnvHealth")
	public RestResponse<EnvHealth> queryEnvHealth(@CookieValue("login_token") String loginToken) {
		return success(envExtApplicationService.queryEnvHealth(queryLoginUserByToken(loginToken)));
	}

	/**
	 * 添加（修改）境健康配置
	 * 
	 * @param envHealthParam 添加（修改）参数
	 * @return 无
	 */
	@PostMapping("/addOrUpdateEnvHealth")
	public RestResponse<Void> addOrUpdateEnvHealth(@RequestBody EnvHealth envHealthParam) {
		return success(envExtApplicationService.addOrUpdateEnvHealth(envHealthParam));
	}
}