package org.dhorse.rest.resource;

import org.dhorse.api.param.project.ProjectCreationParam;
import org.dhorse.api.param.project.ProjectDeletionParam;
import org.dhorse.api.param.project.ProjectPageParam;
import org.dhorse.api.param.project.ProjectQueryParam;
import org.dhorse.api.param.project.ProjectUpdateParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.Project;
import org.dhorse.application.service.ProjectApplicationService;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 项目
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/project")
public class ProjectRest extends AbstractRest {

	@Autowired
	private ProjectApplicationService projectApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param projectPageParam 查询参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<Project>> page(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectPageParam projectPageParam) {
		try {
			return success(projectApplicationService.page(queryLoginUserByToken(loginToken), projectPageParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 查询详情
	 * 
	 * @param projectQueryParam 查询参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/query")
	public RestResponse<Project> query(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectQueryParam projectQueryParam) {
		try {
			return success(projectApplicationService.query(queryLoginUserByToken(loginToken),
					projectQueryParam.getProjectId()));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 添加
	 * 
	 * @param projectCreationParam 添加项目参数
	 * @return 项目编号
	 */
	@PostMapping("/add")
	public RestResponse<Project> add(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectCreationParam projectCreationParam) {
		try {
			return success(projectApplicationService.add(queryLoginUserByToken(loginToken), projectCreationParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 修改
	 * 
	 * @param projectUpdateParam 修改项目参数
	 * @return 无
	 */
	@PostMapping("/update")
	public RestResponse<Void> update(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectUpdateParam projectUpdateParam) {
		try {
			return success(projectApplicationService.update(queryLoginUserByToken(loginToken), projectUpdateParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 删除
	 * 
	 * @param projectDeletionParam 删除项目参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectDeletionParam projectDeletionParam) {
		try {
			return success(projectApplicationService.delete(queryLoginUserByToken(loginToken), projectDeletionParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
}