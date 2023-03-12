package org.dhorse.rest.resource;

import org.dhorse.api.param.app.AppCreationParam;
import org.dhorse.api.param.app.AppDeletionParam;
import org.dhorse.api.param.app.AppPageParam;
import org.dhorse.api.param.app.AppQueryParam;
import org.dhorse.api.param.app.AppUpdateParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.vo.App;
import org.dhorse.application.service.AppApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 应用
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/app")
public class AppRest extends AbstractRest {

	@Autowired
	private AppApplicationService appApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param appPageParam 查询参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<App>> page(@CookieValue("login_token") String loginToken,
			@RequestBody AppPageParam appPageParam) {
		return success(appApplicationService.page(queryLoginUserByToken(loginToken), appPageParam));
	}

	/**
	 * 查询详情
	 * 
	 * @param appQueryParam 查询参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/query")
	public RestResponse<App> query(@CookieValue("login_token") String loginToken,
			@RequestBody AppQueryParam appQueryParam) {
		return success(
				appApplicationService.query(queryLoginUserByToken(loginToken), appQueryParam.getAppId()));
	}

	/**
	 * 添加
	 * 
	 * @param appCreationParam 添加应用参数
	 * @return 应用编号
	 */
	@PostMapping("/add")
	public RestResponse<App> add(@CookieValue("login_token") String loginToken,
			@RequestBody AppCreationParam appCreationParam) {
		return success(appApplicationService.add(queryLoginUserByToken(loginToken), appCreationParam));
	}

	/**
	 * 修改
	 * 
	 * @param appUpdateParam 修改应用参数
	 * @return 无
	 */
	@PostMapping("/update")
	public RestResponse<Void> update(@CookieValue("login_token") String loginToken,
			@RequestBody AppUpdateParam appUpdateParam) {
		return success(appApplicationService.update(queryLoginUserByToken(loginToken), appUpdateParam));
	}

	/**
	 * 删除
	 * 
	 * @param appDeletionParam 删除应用参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody AppDeletionParam appDeletionParam) {
		return success(appApplicationService.delete(queryLoginUserByToken(loginToken), appDeletionParam));
	}
}