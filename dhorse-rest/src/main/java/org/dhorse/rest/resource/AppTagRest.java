package org.dhorse.rest.resource;

import java.util.List;

import org.dhorse.api.param.app.branch.AppBranchListParam;
import org.dhorse.api.param.app.tag.AppTagCreationParam;
import org.dhorse.api.param.app.tag.AppTagDeletionParam;
import org.dhorse.api.param.app.tag.AppTagPageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.response.model.AppBranch;
import org.dhorse.api.response.model.AppTag;
import org.dhorse.application.service.AppBranchApplicationService;
import org.dhorse.application.service.AppTagApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 应用标签
 * 
 * @author 无双
 */
@RestController
@RequestMapping("/app/tag")
public class AppTagRest extends AbstractRest {

	@Autowired
	private AppBranchApplicationService appBranchApplicationService;
	
	@Autowired
	private AppTagApplicationService appTagApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param appBranchPageParam 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<AppTag>> page(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppTagPageParam appTagPageParam) {
		return success(appTagApplicationService.page(queryLoginUserByToken(loginToken),
				appTagPageParam));
	}
	
	/**
	 * 搜索分支
	 * 
	 * @param appBranchListParam 分页参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/search")
	public RestResponse<List<AppBranch>> search(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppBranchListParam appBranchListParam) {
		return success(appBranchApplicationService.list(queryLoginUserByToken(loginToken),
				appBranchListParam));
	}

	/**
	 * 添加
	 * 
	 * @param appTagCreationParam 添加参数
	 * @return 无
	 */
	@PostMapping("/add")
	public RestResponse<Void> add(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppTagCreationParam appTagCreationParam) {
		return success(appTagApplicationService.add(queryLoginUserByToken(loginToken),
				appTagCreationParam));
	}

	/**
	 * 删除
	 * 
	 * @param appTagDeletionParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody AppTagDeletionParam appTagDeletionParam) {
		return success(appTagApplicationService.delete(queryLoginUserByToken(loginToken),
				appTagDeletionParam));
	}
}