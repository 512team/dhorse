package org.dhorse.application.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.dhorse.api.enums.AgentImageSourceEnum;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.global.GlolabConfigDeletionParam;
import org.dhorse.api.param.global.GlolabConfigPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.GlobalConfigAgg.BaseGlobalConfig;
import org.dhorse.api.vo.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.vo.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.vo.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.vo.GlobalConfigAgg.Ldap;
import org.dhorse.api.vo.GlobalConfigAgg.Maven;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.GlobalConfigPO;
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
import org.springframework.util.ResourceUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
			try (InputStream in = resource.getInputStream();
					FileOutputStream out = new FileOutputStream(localPath + "pom.xml")) {
				byte[] buffer = new byte[1024 * 1024];
				int offset = 0;
				while ((offset = in.read(buffer)) != -1) {
					out.write(buffer, 0, offset);
				}
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
				createProject(imageRepo, true);
			}catch(ApplicationException e) {
				//这里为了兼容Harbor2.0接口参数类型的不同，再次调用
				createProject(imageRepo, 1);
			}
		}
		imageRepo.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		return addOrUpdateGlobalConfig(imageRepo);
	}
	
	private void createProject(ImageRepo imageRepo, Object publicType) {
        String uri = "api/v2.0/projects";
        if(!imageRepo.getUrl().endsWith("/")) {
        	uri = "/" + uri;
        }
        HttpPost httpPost = new HttpPost(imageRepo.getUrl() + uri);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setHeader("Authorization", "Basic "+ Base64.getUrlEncoder().encodeToString((imageRepo.getAuthName() + ":" + imageRepo.getAuthPassword()).getBytes()));
        String param = "{\"project_name\": \"dhorse\", \"public\": " + publicType + "}";
        httpPost.setEntity(new StringEntity(param, "UTF-8"));
        try (CloseableHttpResponse response = createHttpClient(imageRepo.getUrl()).execute(httpPost)){
            if (response.getStatusLine().getStatusCode() != 201
            		&& response.getStatusLine().getStatusCode() != 409) {
            	LogUtils.throwException(logger, response.getStatusLine().getReasonPhrase(),
            			MessageCodeEnum.IMAGE_REPO_PROJECT_FAILURE);
            }
        } catch (IOException e) {
        	LogUtils.throwException(logger, e, MessageCodeEnum.IMAGE_REPO_PROJECT_FAILURE);
        }
	}
	
	public Void addOrUpdateLdap(Ldap ldap) {
		ldap.setItemType(GlobalConfigItemTypeEnum.LDAP.getCode());
		return addOrUpdateGlobalConfig(ldap);
	}
	
	public PageData<EnvTemplate> envTemplatePage(GlolabConfigPageParam pageParam) {
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
			addParam.setReplicaCpu(2);
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
	
	public PageData<TraceTemplate> traceTemplatePage(GlolabConfigPageParam pageParam) {
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
	
	private void buildAgentImage(TraceTemplate taceTemplate, GlobalConfigAgg globalConfigAgg) {
		if(!AgentImageSourceEnum.VERSION.getCode().equals(taceTemplate.getAgentImageSource())) {
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
		
		//3.制作Agent镜像并上传到仓库
		System.setProperty("jib.httpTimeout", "10000");
		System.setProperty("sendCredentialsOverHttp", "true");
		ImageRepo imageRepo = globalConfigAgg.getImageRepo();
		String imageName = "skywalking-agent:v" + taceTemplate.getAgentVersion();
		try {
			RegistryImage registryImage = RegistryImage.named(fullNameOfImage(imageRepo, imageName)).addCredential(
					imageRepo.getAuthName(),
					imageRepo.getAuthPassword());
			Jib.from("busybox:latest")
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
	
	public Void delete(GlolabConfigDeletionParam deleteParam) {
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
}
