package org.dhorse.rest.resource;

import org.dhorse.api.param.global.GlolabConfigDeletionParam;
import org.dhorse.api.param.global.GlolabConfigPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.vo.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.vo.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.vo.GlobalConfigAgg.Ldap;
import org.dhorse.api.vo.GlobalConfigAgg.Maven;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.application.service.GlobalConfigApplicationService;
import org.dhorse.infrastructure.annotation.AccessOnlyAdmin;
import org.dhorse.infrastructure.param.GlobalConfigQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 全局配置
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/globalConfig")
public class GlobalConfigRest extends AbstractRest {

	@Autowired
	private GlobalConfigApplicationService globalConfigApplicationService;

	/**
	 * 查询
	 * 
	 * @return 配置数据
	 */
	@AccessOnlyAdmin
	@PostMapping("/query")
	public RestResponse<GlobalConfigAgg> query(@RequestBody GlobalConfigQueryParam queryParam) {
		return this.success(globalConfigApplicationService.globalConfig(queryParam));
	}

	/**
	 * 添加或修改maven
	 * 
	 * @param mavenConf maven参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/maven/addOrUpdate")
	public RestResponse<Void> maven(@RequestBody Maven mavenConf) {
		return this.success(globalConfigApplicationService.addOrUpdateMaven(mavenConf));
	}

	/**
	 * 添加或修改镜像仓库
	 * 
	 * @param imageRepo 镜像仓库参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/imageRepo/addOrUpdate")
	public RestResponse<Void> imageRepo(@RequestBody ImageRepo imageRepo) {
		return this.success(globalConfigApplicationService.addOrUpdateImageRepo(imageRepo));
	}

	/**
	 * 添加或修改Ldap
	 * 
	 * @param ldap Ldap参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/ldap/addOrUpdate")
	public RestResponse<Void> ldap(@RequestBody Ldap ldap) {
		return this.success(globalConfigApplicationService.addOrUpdateLdap(ldap));
	}

	/**
	 * 添加或修改代码仓库
	 * 
	 * @param codeRepo 代码仓库参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/codeRepo/addOrUpdate")
	public RestResponse<Void> codeRepo(@RequestBody CodeRepo codeRepo) {
		return this.success(globalConfigApplicationService.addOrUpdateCodeRepo(codeRepo));
	}

	/**
	 * 分页查询链路追踪模板
	 * 
	 * @param pageParam 全局配置分页参数
	 * @return 无
	 */
	@PostMapping("/traceTemplate/page")
	public RestResponse<PageData<TraceTemplate>> traceTemplatePage(@RequestBody GlolabConfigPageParam pageParam) {
		return this.success(globalConfigApplicationService.traceTemplatePage(pageParam));
	}

	/**
	 * 添加链路追踪模板
	 * 
	 * @param taceTemplate 追踪模板参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/traceTemplate/add")
	public RestResponse<Void> traceTemplateAdd(@RequestBody TraceTemplate taceTemplate) {
		return this.success(globalConfigApplicationService.addTraceTemplate(taceTemplate));
	}

	/**
	 * 修改链路追踪模板
	 * 
	 * @param taceTemplate 追踪模板参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/traceTemplate/update")
	public RestResponse<Void> traceTemplateUpdate(@RequestBody TraceTemplate taceTemplate) {
		return this.success(globalConfigApplicationService.updateTraceTemplate(taceTemplate));
	}

	/**
	 * 删除配置
	 * 
	 * @param deleteParam 删除参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/traceTemplate/delete")
	public RestResponse<Void> delete(@RequestBody GlolabConfigDeletionParam deleteParam) {
		return this.success(globalConfigApplicationService.delete(deleteParam));
	}

	/**
	 * 分页查询环境模板
	 * 
	 * @param pageParam 全局配置分页参数
	 * @return 无
	 */
	@PostMapping("/envTemplate/page")
	public RestResponse<PageData<EnvTemplate>> envTemplatePage(@RequestBody GlolabConfigPageParam pageParam) {
		return this.success(globalConfigApplicationService.envTemplatePage(pageParam));
	}

	/**
	 * 添加环境模板
	 * 
	 * @param envTemplate 环境模板参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/envTemplate/add")
	public RestResponse<Void> envTemplateAdd(@RequestBody EnvTemplate envTemplate) {
		return this.success(globalConfigApplicationService.addEnvTemplate(envTemplate));
	}

	/**
	 * 修改环境模板
	 * 
	 * @param envTemplate 环境模板参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/envTemplate/update")
	public RestResponse<Void> envTemplateUpdate(@RequestBody EnvTemplate envTemplate) {
		return this.success(globalConfigApplicationService.updateEnvTemplate(envTemplate));
	}
	
	/**
	 * 查询模板
	 * 
	 * @return 配置数据
	 */
	@AccessOnlyAdmin
	@PostMapping("/envTemplate/query")
	public RestResponse<GlobalConfigAgg> envTemplateQuery(@RequestBody GlobalConfigQueryParam queryParam) {
		return this.success(globalConfigApplicationService.envTemplateQuery(queryParam));
	}
}