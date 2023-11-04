package org.dhorse.rest.resource;

import java.util.List;

import org.dhorse.api.param.global.GlobalConfigDeletionParam;
import org.dhorse.api.param.global.GlobalConfigPageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.CustomizedMenu;
import org.dhorse.api.response.model.GlobalConfigAgg.DingDing;
import org.dhorse.api.response.model.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.Ldap;
import org.dhorse.api.response.model.GlobalConfigAgg.Maven;
import org.dhorse.api.response.model.GlobalConfigAgg.More;
import org.dhorse.api.response.model.GlobalConfigAgg.CAS;
import org.dhorse.api.response.model.GlobalConfigAgg.TraceTemplate;
import org.dhorse.api.response.model.GlobalConfigAgg.WeChat;
import org.dhorse.application.service.GlobalConfigApplicationService;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.dhorse.infrastructure.annotation.AccessOnlyAdmin;
import org.dhorse.infrastructure.param.GlobalConfigQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
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
	 * 查询CAS
	 * 
	 * @return CAS数据
	 */
	@AccessNotLogin
	@PostMapping("/query/cas")
	public RestResponse<CAS> queryCas() {
		return this.success(globalConfigApplicationService.queryCas());
	}
	
	/**
	 * 查询Ldap
	 * 
	 * @return Ldap数据
	 */
	@AccessNotLogin
	@PostMapping("/query/ldap")
	public RestResponse<Ldap> queryLdap() {
		return this.success(globalConfigApplicationService.queryLdap());
	}

	/**
	 * 查询企业微信
	 * 
	 * @return 企业微信数据
	 */
	@AccessNotLogin
	@PostMapping("/query/wechat")
	public RestResponse<WeChat> queryWeChat() {
		return this.success(globalConfigApplicationService.queryWeChat());
	}

	/**
	 * 查询钉钉
	 * 
	 * @return 钉钉数据
	 */
	@AccessNotLogin
	@PostMapping("/query/dingding")
	public RestResponse<DingDing> queryDingDing() {
		return this.success(globalConfigApplicationService.queryDingDing());
	}
	
	/**
	 * 添加或修改CAS
	 * 
	 * @param cas CAS参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/cas/addOrUpdate")
	public RestResponse<Void> ldap(@RequestBody CAS cas) {
		return this.success(globalConfigApplicationService.addOrUpdateCas(cas));
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
	 * 添加或修改企业微信
	 * 
	 * @param wechat WeChat参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/wechat/addOrUpdate")
	public RestResponse<Void> wechat(@RequestBody WeChat wechat) {
		return this.success(globalConfigApplicationService.addOrUpdateWeChat(wechat));
	}

	/**
	 * 添加或修改钉钉
	 * 
	 * @param dingding DingDing参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/dingding/addOrUpdate")
	public RestResponse<Void> wechat(@RequestBody DingDing dingding) {
		return this.success(globalConfigApplicationService.addOrUpdateDingDing(dingding));
	}

	/**
	 * 查询菜单
	 * 
	 * @return 菜单数据
	 */
	@GetMapping("/menu")
	public String menu(@CookieValue(name = "login_token", required = false) String loginToken) {
		return globalConfigApplicationService.menu(queryLoginUserByToken(loginToken));
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
	public RestResponse<PageData<TraceTemplate>> traceTemplatePage(@RequestBody GlobalConfigPageParam pageParam) {
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
	public RestResponse<Void> delete(@RequestBody GlobalConfigDeletionParam deleteParam) {
		return this.success(globalConfigApplicationService.delete(deleteParam));
	}

	/**
	 * 分页查询环境模板
	 * 
	 * @param pageParam 全局配置分页参数
	 * @return 无
	 */
	@PostMapping("/envTemplate/page")
	public RestResponse<PageData<EnvTemplate>> envTemplatePage(@RequestBody GlobalConfigPageParam pageParam) {
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

	/**
	 * 分页查询自义定菜单
	 * 
	 * @param pageParam 全局配置分页参数
	 * @return 无
	 */
	@PostMapping("/customizedMenu/page")
	public RestResponse<PageData<CustomizedMenu>> customizedMenuPage(@RequestBody GlobalConfigPageParam pageParam) {
		return this.success(globalConfigApplicationService.customizedMenuPage(pageParam));
	}

	/**
	 * 添加自义定菜单
	 * 
	 * @param customizedMenu 自义定菜单参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/customizedMenu/add")
	public RestResponse<Void> customizedMenuAdd(@RequestBody CustomizedMenu customizedMenu) {
		return this.success(globalConfigApplicationService.customizedMenuAdd(customizedMenu));
	}

	/**
	 * 修改自义定菜单
	 * 
	 * @param customizedMenu 自义定菜单参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/customizedMenu/update")
	public RestResponse<Void> customizedMenuUpdate(@RequestBody CustomizedMenu customizedMenu) {
		return this.success(globalConfigApplicationService.customizedMenuUpdate(customizedMenu));
	}

	/**
	 * 更多
	 * 
	 * @param more 更多配置参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/more/addOrUpdate")
	public RestResponse<Void> more(@RequestBody More more) {
		return this.success(globalConfigApplicationService.addOrUpdateMore(more));
	}

	/**
	 * 查询Java版本
	 * 
	 * @return Java版本列表
	 */
	@PostMapping("/queryJavaVersion")
	public RestResponse<List<String>> queryJavaVersion() {
		return this.success(globalConfigApplicationService.queryJavaVersion());
	}

	/**
	 * 查询操作系统名称
	 * 
	 * @return 操作系统名称
	 */
	@PostMapping("/queryOsName")
	public RestResponse<String> queryOsName() {
		// return this.success(globalConfigApplicationService.queryOsName());
		return this.success("Windows");
	}
}