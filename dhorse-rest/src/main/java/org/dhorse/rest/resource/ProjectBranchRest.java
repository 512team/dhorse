package org.dhorse.rest.resource;

import java.util.List;

import org.dhorse.api.param.project.branch.ProjectBranchCreationParam;
import org.dhorse.api.param.project.branch.ProjectBranchDeletionParam;
import org.dhorse.api.param.project.branch.ProjectBranchListParam;
import org.dhorse.api.param.project.branch.ProjectBranchPageParam;
import org.dhorse.api.param.project.branch.VersionBuildParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.ProjectBranch;
import org.dhorse.application.service.ProjectBranchApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 项目分支
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/project/branch")
public class ProjectBranchRest extends AbstractRest {

	@Autowired
	private ProjectBranchApplicationService projectBranchApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param projectBranchPageParam 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<ProjectBranch>> page(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectBranchPageParam projectBranchPageParam) {
		return success(projectBranchApplicationService.page(queryLoginUserByToken(loginToken),
				projectBranchPageParam));
	}
	
	/**
	 * 搜索分支
	 * 
	 * @param projectBranchListParam 分页参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/search")
	public RestResponse<List<ProjectBranch>> search(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectBranchListParam projectBranchListParam) {
		return success(projectBranchApplicationService.list(queryLoginUserByToken(loginToken),
				projectBranchListParam));
	}

	/**
	 * 添加
	 * 
	 * @param projectBranchCreationParam 添加项目参数
	 * @return 无
	 */
	@PostMapping("/add")
	public RestResponse<Void> add(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectBranchCreationParam projectBranchCreationParam) {
		return success(projectBranchApplicationService.add(queryLoginUserByToken(loginToken),
				projectBranchCreationParam));
	}

	/**
	 * 删除
	 * 
	 * @param projectBranchDeletionParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody ProjectBranchDeletionParam projectBranchDeletionParam) {
		return success(projectBranchApplicationService.delete(queryLoginUserByToken(loginToken),
				projectBranchDeletionParam));
	}

	/**
	 * 构建版本
	 * 
	 * @param versionBuildParam 参数
	 * @return 无
	 */
	@RequestMapping("/buildVersion")
	public RestResponse<String> buildVersion(@CookieValue("login_token") String loginToken,
			@RequestBody VersionBuildParam versionBuildParam) {
		return this.success(projectBranchApplicationService
				.buildVersion(this.queryLoginUserByToken(loginToken), versionBuildParam));
	}
}