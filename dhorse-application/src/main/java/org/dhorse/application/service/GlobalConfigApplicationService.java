package org.dhorse.application.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.ImageSourceEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.global.GlobalConfigDeletionParam;
import org.dhorse.api.param.global.GlobalConfigPageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.GlobalConfigAgg.BaseGlobalConfig;
import org.dhorse.api.response.model.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.CustomizedMenu;
import org.dhorse.api.response.model.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.Ldap;
import org.dhorse.api.response.model.GlobalConfigAgg.Maven;
import org.dhorse.api.response.model.GlobalConfigAgg.More;
import org.dhorse.api.response.model.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.model.Menu;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.GlobalConfigPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.FileUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.Jib;
import com.google.cloud.tools.jib.api.LogEvent;
import com.google.cloud.tools.jib.api.RegistryImage;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;

/**
 * 
 * 全局配置应用服务
 * 
 * @author 天地之怪
 */
@Service
public class GlobalConfigApplicationService extends DeployApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(GlobalConfigApplicationService.class);

	public String menu(LoginUser loginUser) {
		String menuPath = Constants.DHORSE_HOME + "/static/api/";
		if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			menuPath += "init_admin.json";
		}else {
			menuPath += "init_normal.json";
		}
		String menuStr = null;
		try {
			menuStr = FileUtils.readFileToString(new File(menuPath), "UTF-8");
		} catch (IOException e) {
			logger.error("Faile to read menu file", e);
		}
		
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setItemType(GlobalConfigItemTypeEnum.CUSTOMIZED_MENU.getCode());
		List<GlobalConfigPO> menus = globalConfigRepository.list(bizParam);
		if(CollectionUtils.isEmpty(menus)) {
			return menuStr;
		}
		
		String style = "fa fa-external-link";
		Map<String, Menu> parentMenus = new HashMap<>();
		for(int i = 0; i < menus.size(); i++) {
			GlobalConfigPO c = menus.get(i);
			CustomizedMenu dto = JsonUtils.parseToObject(c.getItemValue(), CustomizedMenu.class);
			Menu m = new Menu();
			m.setTitle(dto.getName());
			m.setHref(dto.getUrl());
			if(i % 2 == 0) {
				m.setIcon(style + "-square");
			}else {
				m.setIcon(style);
			}
			m.setTarget("_blank");
			Menu p = parentMenus.get(dto.getParentName());
			if(p == null) {
				p = new Menu();
				p.setTitle(dto.getParentName());
				p.setHref("");
				p.setIcon("fa fa-angle-double-right");
				p.setTarget("_self");
				parentMenus.put(dto.getParentName(), p);
			}
			p.addChild(m);
		}
		JsonNode node = JsonUtils.parseToNode(menuStr);
		ArrayNode menuInfo = ((ArrayNode)node.get("menuInfo"));
		for(Entry<String, Menu> e : parentMenus.entrySet()) {
			menuInfo.addPOJO(e.getValue());
		}
		return node.toString();
	}
	
	public Void addOrUpdateMaven(Maven mavenConf) {
		mavenConf.setItemType(GlobalConfigItemTypeEnum.MAVEN.getCode());
		addOrUpdateGlobalConfig(mavenConf);
		//如果有新增或者修改maven配置，则重新拉取本地仓库文件
		ThreadPoolUtils.async(new MavenRepo(mavenConf));
		return null;
	}

	private Void addOrUpdateGlobalConfig(BaseGlobalConfig baseConfig) {
		GlobalConfigParam param = new GlobalConfigParam();
		param.setId(baseConfig.getId());
		param.setItemType(baseConfig.getItemType());
		GlobalConfigPO globalConfigPO = globalConfigRepository.query(param);
		param.setItemValue(JsonUtils.toJsonString(baseConfig, "id", "pageSize", "itemType"));
		if(globalConfigPO == null) {
			globalConfigRepository.add(param);
		}else {
			globalConfigRepository.update(param);
		}
		return null;
	}
	
	private class MavenRepo implements Runnable {

		private Maven mavenConf;

		public MavenRepo(Maven mavenConf) {
			this.mavenConf = mavenConf;
		}

		@Override
		public void run() {
			String localPathName = componentConstants.getDataPath() + "app/app_tmp/";
			File localPathOfPom = new File(localPathName);
			if (!localPathOfPom.exists()) {
				localPathOfPom.mkdirs();
			}
			buildTmpApp(localPathName);
			doMavenPack(mavenConf, localPathName);
			deleteTmpApp(localPathName);
		}

		private void buildTmpApp(String localPath) {
			Resource resource = new PathMatchingResourcePatternResolver()
					.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "maven/app_tmp_pom.xml");
			try (InputStream in = resource.getInputStream()) {
				FileUtils.copyInputStreamToFile(in, new File(localPath + "pom.xml"));
			} catch (IOException e) {
				logger.error("Failed to build tmp app", e);
			}
		}
		
		private void deleteTmpApp(String localPath) {
			try {
				FileUtils.deleteDirectory(new File(localPath));
			} catch (IOException e) {
				logger.error("Failed to delete tmp app", e);
			}
		}
	}
	
	public Void addOrUpdateCodeRepo(CodeRepo codeRepo) {
		if(!codeRepo.getUrl().startsWith("http")) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "仓库地址格式不正确");
		}
		codeRepo.setItemType(GlobalConfigItemTypeEnum.CODEREPO.getCode());
		return addOrUpdateGlobalConfig(codeRepo);
	}
	
	public Void addOrUpdateImageRepo(ImageRepo imageRepo) {
		if(!imageRepo.getUrl().startsWith("http")) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "仓库地址格式不正确");
		}
		if(ImageRepoTypeEnum.HARBOR.getValue().equals(imageRepo.getType())) {
			try {
				createProject(imageRepo, false);
			}catch(ApplicationException e) {
				//这里为了兼容Harbor2.0接口参数类型的不同，再次调用
				createProject(imageRepo, 0);
			}
		}
		
		//创建镜像仓库认证key
		List<ClusterPO> clusters = clusterRepository.listAll();
		if(!CollectionUtils.isEmpty(clusters)) {
			for(ClusterPO c : clusters) {
				ClusterStrategy cluster = clusterStrategy(c.getClusterType());
				cluster.createSecret(c, imageRepo);
			}
		}
		
		//构建DHorseAgent镜像
		ThreadPoolUtils.async(() -> {
			buildDHorseAgentImage();
		});
		
		imageRepo.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		return addOrUpdateGlobalConfig(imageRepo);
	}
	
	public Void addOrUpdateLdap(Ldap ldap) {
		ldap.setItemType(GlobalConfigItemTypeEnum.LDAP.getCode());
		return addOrUpdateGlobalConfig(ldap);
	}
	
	public PageData<EnvTemplate> envTemplatePage(GlobalConfigPageParam pageParam) {
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setPageNum(pageParam.getPageNum());
		bizParam.setPageSize(pageParam.getPageSize());
		bizParam.setItemType(pageParam.getItemType());
		IPage<GlobalConfigPO> pagePO = globalConfigRepository.page(bizParam);
		if(pagePO.getTotal() == 0) {
			return zeroPageData(pageParam.getPageSize());
		}
		List<EnvTemplate> resultPage = pagePO.getRecords().stream().map(e ->{
			EnvTemplate template = JsonUtils.parseToObject(e.getItemValue(), EnvTemplate.class);
			template.setId(e.getId());
			template.setItemType(e.getItemType());
			return template;
		}).collect(Collectors.toList());
		return this.pageData(pagePO, resultPage);
	}
	
	public Void addEnvTemplate(EnvTemplate envTemplate) {
		initEnvTemplate(envTemplate);
		valideTemplateParam(envTemplate);
		GlobalConfigParam param = new GlobalConfigParam();
		param.setItemType(GlobalConfigItemTypeEnum.ENV_TEMPLATE.getCode());
		param.setItemValue(JsonUtils.toJsonString(envTemplate, "id", "pageSize", "itemType"));
		globalConfigRepository.add(param);
		return null;
	}
	
	private void initEnvTemplate(EnvTemplate addParam) {
		if(Objects.isNull(addParam.getDeploymentOrder())){
			addParam.setDeploymentOrder(0);
		}
		if(Objects.isNull(addParam.getMinReplicas())){
			addParam.setMinReplicas(1);
		}
		if(Objects.isNull(addParam.getMaxReplicas())){
			addParam.setMaxReplicas(1);
		}
		if(Objects.isNull(addParam.getReplicaCpu())){
			addParam.setReplicaCpu(1000);
		}
		if(Objects.isNull(addParam.getReplicaMemory())){
			addParam.setReplicaMemory(1024);
		}
		if(Objects.isNull(addParam.getAutoScalingCpu())){
			addParam.setAutoScalingCpu(80);
		}
		if(Objects.isNull(addParam.getAutoScalingMemory())){
			addParam.setAutoScalingMemory(80);
		}
		if(Objects.isNull(addParam.getRequiredDeployApproval())){
			addParam.setRequiredDeployApproval(0);
		}
		if(Objects.isNull(addParam.getRequiredMerge())){
			addParam.setRequiredMerge(0);
		}
		if(Objects.isNull(addParam.getTraceStatus())){
			addParam.setTraceStatus(0);
		}
	}
	
	public Void updateEnvTemplate(EnvTemplate envTemplate) {
		if(envTemplate.getId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.ID_IS_EMPTY);
		}
		valideTemplateParam(envTemplate);
		GlobalConfigParam param = new GlobalConfigParam();
		param.setId(envTemplate.getId());
		param.setItemType(GlobalConfigItemTypeEnum.ENV_TEMPLATE.getCode());
		param.setItemValue(JsonUtils.toJsonString(envTemplate, "id", "pageSize", "itemType"));
		globalConfigRepository.update(param);
		return null;
	}
	
	private void valideTemplateParam(EnvTemplate envTemplate) {
		if(envTemplate.getEnvName().length() > 16) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "环境名称不能大于16个字符");
		}
		if(envTemplate.getTag().length() > 16) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "环境标识不能大于16个字符");
		}
		if(envTemplate.getJvmArgs() != null && envTemplate.getJvmArgs().length() > 1024) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "Jvm参数不能大于1024个字符");
		}
		if(envTemplate.getDescription() != null && envTemplate.getDescription().length() > 128) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "环境描述不能大于128个字符");
		}
	}
	
	public PageData<TraceTemplate> traceTemplatePage(GlobalConfigPageParam pageParam) {
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setPageNum(pageParam.getPageNum());
		bizParam.setPageSize(pageParam.getPageSize());
		bizParam.setItemType(pageParam.getItemType());
		IPage<GlobalConfigPO> pagePO = globalConfigRepository.page(bizParam);
		if(pagePO.getTotal() == 0) {
			return zeroPageData(pageParam.getPageSize());
		}
		List<TraceTemplate> resultPage = pagePO.getRecords().stream().map(e ->{
			TraceTemplate template = JsonUtils.parseToObject(e.getItemValue(), TraceTemplate.class);
			template.setId(e.getId());
			template.setItemType(e.getItemType());
			return template;
		}).collect(Collectors.toList());
		return this.pageData(pagePO, resultPage);
	}
	
	public Void addTraceTemplate(TraceTemplate taceTemplate) {
		checkTraceTemplateParam(taceTemplate);
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(bizParam);
		if(globalConfigAgg.getImageRepo() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.IMAGE_REPO_IS_EMPTY);
		}
		
		//制作Agent镜像
		ThreadPoolUtils.async(() -> {
			buildAgentImage(taceTemplate, globalConfigAgg);
		});
		
		GlobalConfigParam param = new GlobalConfigParam();
		param.setItemType(GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode());
		param.setItemValue(JsonUtils.toJsonString(taceTemplate, "id", "pageSize", "itemType"));
		globalConfigRepository.add(param);
		return null;
	}
	
	public Void updateTraceTemplate(TraceTemplate taceTemplate) {
		if(taceTemplate.getId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.ID_IS_EMPTY);
		}
		checkTraceTemplateParam(taceTemplate);
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(bizParam);
		if(globalConfigAgg.getImageRepo() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.IMAGE_REPO_IS_EMPTY);
		}
		
		//制作Agent镜像
		ThreadPoolUtils.async(() -> {
			buildAgentImage(taceTemplate, globalConfigAgg);
		});
		
		GlobalConfigParam param = new GlobalConfigParam();
		param.setId(taceTemplate.getId());
		param.setItemType(GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode());
		param.setItemValue(JsonUtils.toJsonString(taceTemplate, "id", "pageSize", "itemType"));
		globalConfigRepository.update(param);
		return null;
	}
	
	private void checkTraceTemplateParam(TraceTemplate taceTemplate) {
		if(StringUtils.isBlank(taceTemplate.getName())) {
			LogUtils.throwException(logger, MessageCodeEnum.TEMPLATE_NAME_IS_EMPTY);
		}
		if(StringUtils.isBlank(taceTemplate.getServiceUrl())) {
			LogUtils.throwException(logger, MessageCodeEnum.SERVICE_URL_IS_EMPTY);
		}
		if(null == taceTemplate.getTechType()) {
			LogUtils.throwException(logger, MessageCodeEnum.AGENT_TECH_TYPE_IS_EMPTY);
		}
		if(taceTemplate.getServiceUrl().startsWith("http")) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "服务地址格式不正确");
		}
	}
	
	private void checkCustomizedMenu(CustomizedMenu customizedMenu) {
		if(StringUtils.isBlank(customizedMenu.getParentName())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "父级菜单名称不能为空");
		}
		if(StringUtils.isBlank(customizedMenu.getName())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "菜单名称不能为空");
		}
		if(StringUtils.isBlank(customizedMenu.getUrl())) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "菜单地址不能为空");
		}
	}
	
	private void buildAgentImage(TraceTemplate taceTemplate, GlobalConfigAgg globalConfigAgg) {
		if(!ImageSourceEnum.VERSION.getCode().equals(taceTemplate.getAgentImageSource())) {
			return;
		}
		
		//1.下载Agent文件
		String fileName = "apache-skywalking-java-agent-"
				+ taceTemplate.getAgentVersion()
				+ ".tgz";
		String fileUrl = "https://archive.apache.org/dist/skywalking/java-agent/"
				+ taceTemplate.getAgentVersion()
				+ "/"
				+ fileName;
		long current = System.currentTimeMillis();
		File localFilePath = new File(componentConstants.getDataPath() + Constants.RELATIVE_TMP_PATH + "agent/"+ current + "/" + fileName);
		File parentFile = localFilePath.getParentFile();
		parentFile.mkdirs();
		FileUtils.downloadFile(fileUrl, localFilePath);
		
		//2.解压Agent文件
		FileUtils.decompressTarGz(localFilePath, parentFile);
		
		logger.info("Start to build agent image");
		
		//Jib环境变量
		jibProperty();
		
		//3.制作Agent镜像并上传到仓库
		ImageRepo imageRepo = globalConfigAgg.getImageRepo();
		String imageName = "skywalking-agent:v" + taceTemplate.getAgentVersion();
		try {
			RegistryImage registryImage = RegistryImage.named(fullNameOfImage(imageRepo, imageName)).addCredential(
					imageRepo.getAuthName(),
					imageRepo.getAuthPassword());
			Jib.from(Constants.BUSYBOX_IMAGE_URL)
				.addLayer(Arrays.asList(Paths.get(parentFile + "/skywalking-agent")), AbsoluteUnixPath.get("/"))
				.containerize(Containerizer.to(registryImage)
						.setAllowInsecureRegistries(true)
						.addEventHandler(LogEvent.class, logEvent -> logger.info(logEvent.getMessage())));
		} catch (Exception e) {
			logger.error("Failed to build agent image", e);
		}finally {
			try {
				FileUtils.deleteDirectory(parentFile);
			} catch (IOException e) {
				logger.error("Failed to delete agent file", e);
			}
			logger.info("End to build agent image");
		}
	}
	
	public Void addOrUpdateMore(More more) {
		if(StringUtils.isBlank(more.getEventNotifyUrl())){
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "事件通知地址不能为空");
		}
		if(!more.getEventNotifyUrl().startsWith("http")) {
			throw new ApplicationException(MessageCodeEnum.INVALID_PARAM.getCode(), "事件通知地址格式不正确");
		}
		more.setItemType(GlobalConfigItemTypeEnum.MORE.getCode());
		return addOrUpdateGlobalConfig(more);
	}
	
	public Void delete(GlobalConfigDeletionParam deleteParam) {
		GlobalConfigPO globalConfigPO = globalConfigRepository.queryById(deleteParam.getId());
		if(GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode().equals(globalConfigPO.getItemType())) {
			AppEnvParam appEnvParam = new AppEnvParam();
			appEnvParam.setTraceTemplateId(deleteParam.getId());
			appEnvParam.setTraceStatus(YesOrNoEnum.YES.getCode());
			if(appEnvRepository.query(appEnvParam) != null) {
				LogUtils.throwException(logger, MessageCodeEnum.CONFIG_IS_USING);
			}
		}
		globalConfigRepository.delete(deleteParam.getId());
		return null;
	}
	
	public PageData<CustomizedMenu> customizedMenuPage(GlobalConfigPageParam pageParam) {
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setPageNum(pageParam.getPageNum());
		bizParam.setPageSize(pageParam.getPageSize());
		bizParam.setItemType(pageParam.getItemType());
		IPage<GlobalConfigPO> pagePO = globalConfigRepository.page(bizParam);
		if(pagePO.getTotal() == 0) {
			return zeroPageData(pageParam.getPageSize());
		}
		List<CustomizedMenu> resultPage = pagePO.getRecords().stream().map(e ->{
			CustomizedMenu dto = JsonUtils.parseToObject(e.getItemValue(), CustomizedMenu.class);
			dto.setId(e.getId());
			dto.setItemType(e.getItemType());
			return dto;
		}).collect(Collectors.toList());
		return this.pageData(pagePO, resultPage);
	}
	
	public Void customizedMenuAdd(CustomizedMenu customizedMenu) {
		checkCustomizedMenu(customizedMenu);
		GlobalConfigParam param = new GlobalConfigParam();
		param.setItemType(GlobalConfigItemTypeEnum.CUSTOMIZED_MENU.getCode());
		param.setItemValue(JsonUtils.toJsonString(customizedMenu, "id", "pageSize", "itemType"));
		globalConfigRepository.add(param);
		return null;
	}
	
	public Void customizedMenuUpdate(CustomizedMenu customizedMenu) {
		if(customizedMenu.getId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.ID_IS_EMPTY);
		}
		checkCustomizedMenu(customizedMenu);
		GlobalConfigParam param = new GlobalConfigParam();
		param.setId(customizedMenu.getId());
		param.setItemType(GlobalConfigItemTypeEnum.CUSTOMIZED_MENU.getCode());
		param.setItemValue(JsonUtils.toJsonString(customizedMenu, "id", "pageSize", "itemType"));
		globalConfigRepository.update(param);
		return null;
	}
}
