package org.dhorse.rest.resource;

import java.util.List;

import org.dhorse.api.param.app.env.AppEnvCreationParam;
import org.dhorse.api.param.app.env.AppEnvDeletionParam;
import org.dhorse.api.param.app.env.AppEnvPageParam;
import org.dhorse.api.param.app.env.AppEnvQueryParam;
import org.dhorse.api.param.app.env.AppEnvResoureUpdateParam;
import org.dhorse.api.param.app.env.AppEnvSearchParam;
import org.dhorse.api.param.app.env.AppEnvUpdateParam;
import org.dhorse.api.param.app.env.TraceUpdateParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.vo.AppEnv;
import org.dhorse.application.service.AppEnvApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 应用环境
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/app/env")
public class AppEnvRest extends AbstractRest {

	@Autowired
	private AppEnvApplicationService appEnvApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param appEnvPageParam 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<AppEnv>> page(@CookieValue("login_token") String loginToken,
			@RequestBody AppEnvPageParam appEnvPageParam) {
		return success(appEnvApplicationService.page(queryLoginUserByToken(loginToken), appEnvPageParam));
	}

	/**
	 * 搜索列表
	 * 
	 * @param appEnvSearchParam 搜索参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/search")
	public RestResponse<List<AppEnv>> search(@CookieValue("login_token") String loginToken,
			@RequestBody AppEnvSearchParam appEnvSearchParam) {
		return success(appEnvApplicationService.search(queryLoginUserByToken(loginToken), appEnvSearchParam));
	}

	/**
	 * 查询
	 * 
	 * @param appEnvPageParam 查询
	 * @return 符合条件的数据
	 */
	@PostMapping("/query")
	public RestResponse<AppEnv> query(@CookieValue("login_token") String loginToken,
			@RequestBody AppEnvQueryParam appEnvQueryParam) {
		return success(appEnvApplicationService.query(queryLoginUserByToken(loginToken), appEnvQueryParam));
	}

	/**
	 * 添加
	 * 
	 * @param appEnvCreattionParam 添加应用参数
	 * @return 无
	 */
	@PostMapping("/add")
	public RestResponse<Void> add(@RequestBody AppEnvCreationParam appEnvCreattionParam) {
		return success(appEnvApplicationService.add(appEnvCreattionParam));
	}

	/**
	 * 修改
	 * 
	 * @param appEnvUpdateParam 修改参数
	 * @return 无
	 */
	@PostMapping("/update")
	public RestResponse<Void> update(@CookieValue("login_token") String loginToken,
			@RequestBody AppEnvUpdateParam appEnvUpdateParam) {
		return success(appEnvApplicationService.update(queryLoginUserByToken(loginToken), appEnvUpdateParam));
	}

	/**
	 * 修改资源
	 * 
	 * @param envResoureUpdateParam 修改参数
	 * @return 无
	 */
	@PostMapping("/updateResource")
	public RestResponse<Void> updateResource(@CookieValue("login_token") String loginToken,
			@RequestBody AppEnvResoureUpdateParam envResoureUpdateParam) {
		return success(
				appEnvApplicationService.updateResource(queryLoginUserByToken(loginToken), envResoureUpdateParam));
	}

	/**
	 * 设置链路跟踪状态
	 * 
	 * @param openTraceParam 修改参数
	 * @return 无
	 */
	@PostMapping("/updateTrace")
	public RestResponse<Void> updateTrace(@CookieValue("login_token") String loginToken,
			@RequestBody TraceUpdateParam updateTraceParam) {
		return success(appEnvApplicationService.updateTrace(queryLoginUserByToken(loginToken), updateTraceParam));
	}

	/**
	 * 删除
	 * 
	 * @param appEnvDeletionParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody AppEnvDeletionParam appEnvDeletionParam) {
		return success(appEnvApplicationService.delete(queryLoginUserByToken(loginToken), appEnvDeletionParam));
	}

}