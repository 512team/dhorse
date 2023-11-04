package org.dhorse.rest.resource;

import org.dhorse.api.param.app.member.AppMemberCreationParam;
import org.dhorse.api.param.app.member.AppMemberDeletionParam;
import org.dhorse.api.param.app.member.AppMemberPageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.response.model.AppMember;
import org.dhorse.application.service.AppMemberApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 应用成员
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/app/member")
public class AppMemberRest extends AbstractRest {

	@Autowired
	private AppMemberApplicationService appMemberApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param pageQueryParam 查询参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<AppMember>> page(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppMemberPageParam pageQueryParam) {
		return success(appMemberApplicationService.page(queryLoginUserByToken(loginToken), pageQueryParam));
	}

	/**
	 * 添加或修改
	 * 
	 * @param addOrUpdateParam 添加或修改参数
	 * @return 无
	 */
	@PostMapping("/addOrUpdate")
	public RestResponse<Void> addOrUpdate(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppMemberCreationParam addOrUpdateParam) {
		return success(
				appMemberApplicationService.addOrUpdate(queryLoginUserByToken(loginToken), addOrUpdateParam));
	}

	/**
	 * 删除
	 * 
	 * @param deletedParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppMemberDeletionParam deletedParam) {
		return success(appMemberApplicationService.delete(queryLoginUserByToken(loginToken), deletedParam));
	}
}