package org.dhorse.rest.resource;

import org.dhorse.api.param.project.member.ProjectMemberCreationParam;
import org.dhorse.api.param.project.member.ProjectMemberDeletionParam;
import org.dhorse.api.param.project.member.ProjectMemberPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.ProjectMember;
import org.dhorse.application.service.ProjectMemberApplicationService;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 项目成员
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/project/member")
public class ProjectMemberRest extends AbstractRest {

	@Autowired
	private ProjectMemberApplicationService projectMemberApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param pageQueryParam 查询参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<ProjectMember>> page(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectMemberPageParam pageQueryParam) {
		try {
			return success(projectMemberApplicationService.page(queryLoginUserByToken(loginToken), pageQueryParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 添加或修改
	 * 
	 * @param addOrUpdateParam 添加或修改参数
	 * @return 无
	 */
	@PostMapping("/addOrUpdate")
	public RestResponse<Void> addOrUpdate(@CookieValue("login_token") String loginToken, @RequestBody ProjectMemberCreationParam addOrUpdateParam) {
		try {
			return success(projectMemberApplicationService.addOrUpdate(queryLoginUserByToken(loginToken), addOrUpdateParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 删除
	 * 
	 * @param deletedParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectMemberDeletionParam deletedParam) {
		try {
			return success(projectMemberApplicationService.delete(queryLoginUserByToken(loginToken), deletedParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
}