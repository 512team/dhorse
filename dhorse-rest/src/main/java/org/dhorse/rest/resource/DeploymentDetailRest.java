package org.dhorse.rest.resource;

import org.dhorse.api.param.project.branch.deploy.DeploymentApprovementParam;
import org.dhorse.api.param.project.branch.deploy.DeploymentDetailDeletionParam;
import org.dhorse.api.param.project.branch.deploy.DeploymentDetailPageParam;
import org.dhorse.api.param.project.branch.deploy.RollbackApplicationParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.DeploymentDetail;
import org.dhorse.application.service.DeploymentDetailApplicationService;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 部署历史
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/project/deployment/detail")
public class DeploymentDetailRest extends AbstractRest {

	@Autowired
	private DeploymentDetailApplicationService deploymentDetailApplicationService;

	/**
	 * 分页查询
	 * 
	 * @param deploymentDetailPageParam 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<DeploymentDetail>> page(@CookieValue("login_token") String loginToken,
			@RequestBody DeploymentDetailPageParam deploymentDetailPageParam) {
		try {
			return success(deploymentDetailApplicationService.page(queryLoginUserByToken(loginToken),
					deploymentDetailPageParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 审批部署
	 * 
	 * @param deploymentApplicationParam 审批参数
	 * @return 审批后的明细编号
	 */
	@RequestMapping("/approveToDeploy")
	public RestResponse<Void> approveToDeploy(@CookieValue("login_token") String loginToken,
			@RequestBody DeploymentApprovementParam deploymentApplicationParam) {
		try {
			return this.success(deploymentDetailApplicationService
					.approveToDeploy(queryLoginUserByToken(loginToken), deploymentApplicationParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 提交回滚
	 * 
	 * @param requestParam 提交参数
	 * @return 提交回滚的参数
	 */
	@RequestMapping("/submitToRollback")
	public RestResponse<Void> submitToRollback(@CookieValue("login_token") String loginToken,
			@RequestBody RollbackApplicationParam requestParam) {
		try {
			return this.success(deploymentDetailApplicationService
					.submitToRollback(queryLoginUserByToken(loginToken), requestParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 删除
	 * 
	 * @param requestParam 删除参数
	 * @return 无
	 */
	@RequestMapping("/delete")
	public RestResponse<Void> delete(@CookieValue("login_token") String loginToken,
			@RequestBody DeploymentDetailDeletionParam requestParam) {
		try {
			return this.success(
					deploymentDetailApplicationService.delete(queryLoginUserByToken(loginToken), requestParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
}