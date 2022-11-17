package org.dhorse.rest.resource;

import org.dhorse.api.param.app.branch.DeploymentApplicationParam;
import org.dhorse.api.param.app.branch.deploy.DeploymentVersionDeletionParam;
import org.dhorse.api.param.app.branch.deploy.DeploymentVersionPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.DeploymentVersion;
import org.dhorse.application.service.DeploymentVersionApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 部署版本
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/app/deployment/version")
public class DeploymentVersionRest extends AbstractRest {

	@Autowired
	private DeploymentVersionApplicationService deploymentVersionApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param deploymentVersionPageParam 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<DeploymentVersion>> page(@CookieValue("login_token") String loginToken,
			@RequestBody DeploymentVersionPageParam deploymentVersionPageParam) {
		return success(deploymentVersionApplicationService.page(queryLoginUserByToken(loginToken),
				deploymentVersionPageParam));
	}
	
	/**
	 * 删除
	 * 
	 * @param deletionParam 删除参数
	 * @return 无
	 */
	@PostMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody DeploymentVersionDeletionParam deletionParam) {
		return success(deploymentVersionApplicationService.delete(queryLoginUserByToken(loginToken),
				deletionParam));
	}
	
	/**
	 * 部署
	 * 
	 * @param deploymentApplictionParam 提交部署参数
	 * @return 无
	 */
	@RequestMapping("/submitToDeploy")
	public RestResponse<Void> submitToDeploy(@CookieValue("login_token") String loginToken,
			@RequestBody DeploymentApplicationParam deploymentApplictionParam) {
		return this.success(deploymentVersionApplicationService
				.submitToDeploy(this.queryLoginUserByToken(loginToken), deploymentApplictionParam));
	}
}