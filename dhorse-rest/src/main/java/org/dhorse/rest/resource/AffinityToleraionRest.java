package org.dhorse.rest.resource;

import org.dhorse.api.param.app.env.affinity.AffinityTolerationCreationParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationDeletionParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationPageParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationQueryParam;
import org.dhorse.api.param.app.env.affinity.AffinityTolerationUpdateParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.vo.AffinityToleration;
import org.dhorse.application.service.AffinityTolerationApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 亲和容忍配置服务
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/app/env/affinity")
public class AffinityToleraionRest extends AbstractRest {

	@Autowired
	private AffinityTolerationApplicationService service;

	/**
	 * 分页查询
	 * 
	 * @param 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<AffinityToleration>> page(@CookieValue("login_token") String loginToken,
			@RequestBody AffinityTolerationPageParam pageParam) {
		return success(service.page(queryLoginUserByToken(loginToken), pageParam));
	}
	
	/**
	 * 查询
	 * 
	 * @param 查询参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/query")
	public RestResponse<AffinityToleration> query(@CookieValue("login_token") String loginToken,
			@RequestBody AffinityTolerationQueryParam queryParam) {
		return success(service.query(queryLoginUserByToken(loginToken), queryParam));
	}

	/**
	 * 添加
	 * 
	 * @param addParam 添加参数
	 * @return 无
	 */
	@PostMapping("/add")
	public RestResponse<Void> add(@RequestBody AffinityTolerationCreationParam addParam) {
		return success(service.add(addParam));
	}

	/**
	 * 修改
	 * 
	 * @param updateParam 修改参数
	 * @return 无
	 */
	@PostMapping("/update")
	public RestResponse<Void> update(@CookieValue("login_token") String loginToken,
			@RequestBody AffinityTolerationUpdateParam updateParam) {
		return success(service.update(queryLoginUserByToken(loginToken), updateParam));
	}

	/**
	 * 修改
	 * 
	 * @param updateParam 修改参数
	 * @return 无
	 */
	@PostMapping("/openStatus")
	public RestResponse<Void> openStatus(@CookieValue("login_token") String loginToken,
			@RequestBody AffinityTolerationUpdateParam updateParam) {
		return success(service.openStatus(queryLoginUserByToken(loginToken), updateParam));
	}
	
	/**
	 * 删除
	 * 
	 * @param deletionParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody AffinityTolerationDeletionParam deletionParam) {
		return success(service.delete(queryLoginUserByToken(loginToken), deletionParam));
	}

}