package org.dhorse.application.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.dhorse.api.enums.ClusterTypeEnum;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.Ldap;
import org.dhorse.api.response.model.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.param.GlobalConfigQueryParam;
import org.dhorse.infrastructure.repository.AffinityTolerationRepository;
import org.dhorse.infrastructure.repository.AppEnvRepository;
import org.dhorse.infrastructure.repository.AppMemberRepository;
import org.dhorse.infrastructure.repository.AppRepository;
import org.dhorse.infrastructure.repository.ClusterRepository;
import org.dhorse.infrastructure.repository.DeploymentDetailRepository;
import org.dhorse.infrastructure.repository.DeploymentVersionRepository;
import org.dhorse.infrastructure.repository.EnvExtRepository;
import org.dhorse.infrastructure.repository.GlobalConfigRepository;
import org.dhorse.infrastructure.repository.MetricsRepository;
import org.dhorse.infrastructure.repository.SysUserRepository;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.BasePO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.cluster.K8sClusterStrategy;
import org.dhorse.infrastructure.strategy.cluster.K8sClusterStrategy2;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.dhorse.infrastructure.utils.FileUtils;
import org.dhorse.infrastructure.utils.HttpUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.Jib;
import com.google.cloud.tools.jib.api.LogEvent;
import com.google.cloud.tools.jib.api.RegistryImage;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;

/**
 * 
 * 应用层基础服务
 * 
 * @author 天地之怪
 */
public abstract class ApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
	
	@Autowired
	protected SysUserRepository sysUserRepository;
	
	@Autowired
	protected GlobalConfigRepository globalConfigRepository;
	
	@Autowired
	protected ClusterRepository clusterRepository;
	
	@Autowired
	protected AppRepository appRepository;
	
	@Autowired
	protected AppMemberRepository appMemberRepository;

	@Autowired
	protected AppEnvRepository appEnvRepository;
	
	@Autowired
	protected EnvExtRepository envExtRepository;
	
	@Autowired
	protected AffinityTolerationRepository affinityTolerationRepository;
	
	@Autowired
	protected DeploymentDetailRepository deploymentDetailRepository;
	
	@Autowired
	protected DeploymentVersionRepository deploymentVersionRepository;
	
	@Autowired
	protected ComponentConstants componentConstants;
	
	@Autowired
	protected MetricsRepository metricsRepository;
	
	protected ApiClient apiClient(String basePath, String accessToken) {
		ApiClient apiClient = new ClientBuilder().setBasePath(basePath).setVerifyingSsl(false)
				.setAuthentication(new AccessTokenAuthentication(accessToken)).build();
		return apiClient;
	}
	
	public GlobalConfigAgg globalConfig() {
		GlobalConfigParam param = new GlobalConfigParam();
		param.setItemTypes(Arrays.asList(GlobalConfigItemTypeEnum.LDAP.getCode(),
				GlobalConfigItemTypeEnum.CODEREPO.getCode(),
				GlobalConfigItemTypeEnum.IMAGEREPO.getCode(),
				GlobalConfigItemTypeEnum.MAVEN.getCode(),
				GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode(),
				GlobalConfigItemTypeEnum.MORE.getCode()));
		return globalConfigRepository.queryAgg(param);
	}
	
	public GlobalConfigAgg globalConfig(GlobalConfigQueryParam queryParam) {
		GlobalConfigParam configParam = new GlobalConfigParam();
		configParam.setId(queryParam.getGlobalConfigId());
		configParam.setItemTypes(queryParam.getItemTypes());
		return globalConfigRepository.queryAgg(configParam);
	}
	
	public Ldap queryLdap() {
		GlobalConfigParam configParam = new GlobalConfigParam();
		configParam.setItemType(GlobalConfigItemTypeEnum.LDAP.getCode());
		return globalConfigRepository.queryAgg(configParam).getLdap();
	}
	
	public GlobalConfigAgg envTemplateQuery(GlobalConfigQueryParam queryParam) {
		GlobalConfigAgg globalConfigAgg = globalConfig(queryParam);
		EnvTemplate template = globalConfigAgg.getEnvTemplate();
		if(template == null) {
			return globalConfigAgg;
		}
		if(!YesOrNoEnum.YES.getCode().equals(template.getTraceStatus())) {
			return globalConfigAgg;
		}
		TraceTemplate traceTemplate = globalConfig().getTraceTemplate(template.getTraceTemplateId());
		if(traceTemplate != null) {
			template.setTraceTemplateName(traceTemplate.getName());
		}
		return globalConfigAgg;
	}
	
	protected String mavenRepo() {
		return componentConstants.getDataPath() + "maven/repository/";
	}
	
	public AppPO validateApp(String appId) {
		AppPO appPO = appRepository.queryById(appId);
		if(appPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_IS_INEXISTENCE);
		}
		return appPO;
	}
	
	protected ClusterStrategy clusterStrategy(Integer clusterType) {
		if (ClusterTypeEnum.K8S.getCode().equals(clusterType)) {
			return new K8sClusterStrategy2();
		} else {
			return new K8sClusterStrategy();
		}
	}
	
	protected void hasRights(LoginUser loginUser, String appId) {
		if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())){
			return;
		}
		AppMemberParam appMemberParam = new AppMemberParam();
		appMemberParam.setAppId(appId);
		appMemberParam.setUserId(loginUser.getId());
		AppMemberPO appMemberPO = appMemberRepository.query(appMemberParam);
		if(Objects.isNull(appMemberPO)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
	}
	
	protected void hasAdminRights(LoginUser loginUser, String appId) {
		if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())){
			return;
		}
		AppMemberParam appMemberParam = new AppMemberParam();
		appMemberParam.setAppId(appId);
		appMemberParam.setUserId(loginUser.getId());
		AppMemberPO appMemberPO = appMemberRepository.query(appMemberParam);
		if(Objects.isNull(appMemberPO)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		String[] roleTypes = appMemberPO.getRoleType().split(",");
		Set<Integer> roleSet = new HashSet<>();
		for (String role : roleTypes) {
			roleSet.add(Integer.valueOf(role));
		}
		List<Integer> adminRole = Constants.ROLE_OF_OPERATE_APP_USER.stream()
				.filter(item -> roleSet.contains(item))
				.collect(Collectors.toList());
		if(adminRole.size() <= 0) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
	}
	
	protected String fullNameOfTraceAgentImage(DeploymentContext context) {
		if(!YesOrNoEnum.YES.getCode().equals(context.getAppEnv().getTraceStatus())) {
			return null;
		}
		TraceTemplate traceTemplate = context.getGlobalConfigAgg().getTraceTemplate(context.getAppEnv().getTraceTemplateId());
		if(!StringUtils.isBlank(traceTemplate.getAgentImage())) {
			return traceTemplate.getAgentImage();
		}
		String imageName = "skywalking-agent:v" + traceTemplate.getAgentVersion();
		return fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(), imageName);
	}
	
	protected String fullNameOfDHorseAgentImage(ImageRepo imageRepo) {
		String imageName = "dhorse-agent:" + componentConstants.getVersion();
		return fullNameOfImage(imageRepo, imageName);
	}
	
	protected String fullNameOfImage(ImageRepo imageRepo, String nameOfImage) {
		if(imageRepo == null) {
			LogUtils.throwException(logger, MessageCodeEnum.IMAGE_REPO_IS_EMPTY);
		}
		String imgUrl = imageRepo.getUrl();
		if(imgUrl.startsWith("http")) {
			imgUrl = imgUrl.substring(imgUrl.indexOf("//") + 2);
		}
		StringBuilder fullNameOfImage = new StringBuilder(imgUrl);
		if(!imgUrl.endsWith("/")) {
			fullNameOfImage.append("/");
		}
		if(ImageRepoTypeEnum.DOCKERHUB.getValue().equals(imageRepo.getType())) {
			fullNameOfImage.append(imageRepo.getAuthName());
		}else {
			fullNameOfImage.append(Constants.DHORSE_TAG);
		}
		fullNameOfImage.append("/").append(nameOfImage);
		return fullNameOfImage.toString();
	}
	
	protected String imageNameOfJdk(String baseImageVersion) {
		return Constants.IMAGE_NAME_JDK + ":" + tagOfJdk(baseImageVersion);
	}
	
	protected String tagOfJdk(String version) {
		return "v" + version;
	}
	
	public String queryJavaMajorVersion(String javaHome){
		List<String> javaVersions = queryJavaVersion(javaHome);
		String[] versions = javaVersions.get(0).split("\\.");
		if(Integer.valueOf(versions[0]) < 9) {
			return versions[0] + "." + versions[1];
		}
		return versions[0];
	}
	
	public List<String> queryJavaVersion(){
		return queryJavaVersion("xxxxxxxxxx");
	}
	
	public List<String> queryJavaVersion(String javaHome){
		if(StringUtils.isBlank(javaHome)) {
			return null;
		}
		
		List<String> javaVersions = new ArrayList<>();
		
		//如果没有配置Maven的Java安装目录，则取DHorse所在的Java版本
		if(StringUtils.isBlank(javaHome)){
			javaVersions.add(System.getProperty("java.version"));
			return javaVersions;
		}
		
		//从指定的Java安装目录中获取版本
		if(!javaHome.endsWith("/")) {
			javaHome = javaHome + "/";
		}
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[]{javaHome + "bin/java", "-version"});
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))){
			String versionStr = br.readLine().split("\\s+")[2].replace("\"", "");
			javaVersions.add(versionStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(process != null) {
			process.destroy();
		}
		return javaVersions;
	}
	
	public String queryOsName() {
		//目前只支持自定义基础镜像
		return "Windows";
		//return System.getProperty("os.name");
	}
	
	protected boolean isWindows() {
		return System.getProperty("os.name").indexOf("Windows") > -1;
	}
	
	protected void doNotify(String url, String reponse) {
		if(StringUtils.isBlank(url)){
			return;
		}
		
		int httpCode = 0;
		for(int i = 0; i < 5; i++) {
			httpCode = HttpUtils.post(url, reponse);
			if(httpCode == 200) {
				logger.info("Successfully to noifty url: {}", url);
				break;
			}
			logger.error("Failed to notify url: {}, http code: {}", url, httpCode);
		}
	}
	
	public void asynInitConfig() {
		ThreadPoolUtils.async(() -> {
			
			//随机休眠几秒，两个目的：
			//1.集群部署时，服务并行启动可能带来的并发操作问题
			//2.休眠30-40秒，可以让部署者有足够的时间看到DHorse服务已经启动完成的日志
			int secods = 30 + new Random().nextInt(10) + 1;
			try {
				Thread.sleep(secods * 1000);
			} catch (InterruptedException e) {
				//ignore
			}
			
			try {
				createDHorseConfig();
			} catch (Exception e) {
				logger.error("Failed to create dhorse config", e);
			}
			
			try {
				createSecret();
			} catch (Exception e) {
				logger.error("Failed to create secret", e);
			}
			
			try {
				creatImageRepoProject();
			} catch (Exception e) {
				logger.error("Failed to creat image repo project", e);
			}
			
			try {
				buildDHorseAgentImage();
			} catch (Exception e) {
				logger.error("Failed to build dhorse agent image", e);
			}
			
			try {
				downloadGradle();
			} catch (Exception e) {
				logger.error("Failed to download gradle", e);
			}
			
			try {
				downloadMaven();
			} catch (Exception e) {
				logger.error("Failed to download maven", e);
			}
			
		});
	}
	
	private void creatImageRepoProject() {
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(globalConfigParam);
		ImageRepo imageRepo = globalConfigAgg.getImageRepo();
		if(imageRepo == null) {
			return;
		}
		if(ImageRepoTypeEnum.HARBOR.getValue().equals(imageRepo.getType())) {
			try {
				createProject(imageRepo, false);
			}catch(ApplicationException e) {
				//这里为了兼容Harbor2.0接口参数类型的不同，再次调用
				createProject(imageRepo, 0);
			}
		}
	}
	
	//写入容器集群的dhorse服务地址
	private void createDHorseConfig() {
		List<ClusterPO> clusters = clusterRepository.listAll();
		if(CollectionUtils.isEmpty(clusters)) {
			return;
		}
		for(ClusterPO c : clusters) {
			ClusterStrategy cluster = clusterStrategy(c.getClusterType());
			cluster.createDHorseConfig(c);
		}
	}
	
	//删除容器集群的dhorse服务地址
	public void deleteDHorseConfig() {
		logger.info("Start to delete dhorse config");
		List<ClusterPO> clusters = clusterRepository.listAll();
		if(CollectionUtils.isEmpty(clusters)) {
			return;
		}
		for(ClusterPO c : clusters) {
			ClusterStrategy cluster = clusterStrategy(c.getClusterType());
			cluster.deleteDHorseConfig(c);
		}
		logger.info("End to delete dhorse config");
	}
	
	//创建镜像仓库认证key
	private void createSecret() {
		List<ClusterPO> clusters = clusterRepository.listAll();
		if(CollectionUtils.isEmpty(clusters)) {
			return;
		}
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(globalConfigParam);
		if(globalConfigAgg == null || globalConfigAgg.getImageRepo() == null) {
			return;
		}
		for(ClusterPO c : clusters) {
			ClusterStrategy cluster = clusterStrategy(c.getClusterType());
			cluster.createSecret(c, globalConfigAgg.getImageRepo());
		}
	}
	
	protected void createProject(ImageRepo imageRepo, Object publicType) {
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
        try (CloseableHttpResponse response = HttpUtils.createHttpClient(imageRepo.getUrl()).execute(httpPost)){
            if (response.getStatusLine().getStatusCode() != 201
            		&& response.getStatusLine().getStatusCode() != 409) {
            	LogUtils.throwException(logger, response.getStatusLine().getReasonPhrase(),
            			MessageCodeEnum.IMAGE_REPO_PROJECT_FAILURE);
            }
        } catch (IOException e) {
        	LogUtils.throwException(logger, e, MessageCodeEnum.IMAGE_REPO_PROJECT_FAILURE);
        }
	}
	
	protected void buildDHorseAgentImage() {
		GlobalConfigParam globalConfigParam = new GlobalConfigParam();
		globalConfigParam.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(globalConfigParam);
		if(globalConfigAgg == null || globalConfigAgg.getImageRepo() == null) {
			logger.info("The image repo does not exist, and end to build dhorse agent image");
			return;
		}
		buildDHorseAgentImage(globalConfigAgg.getImageRepo());
	}
	
	protected void buildDHorseAgentImage(ImageRepo imageRepo) {
		logger.info("Start to build jvm metrics agent image");
		
		//Jib环境变量
		jibProperty();
		
		String javaAgentPath = Constants.DHORSE_HOME + "/lib/ext/dhorse-agent-"+ componentConstants.getVersion() +".jar";
		if(!new File(javaAgentPath).exists()) {
			javaAgentPath = Constants.DHORSE_HOME + "/dhorse-agent/target/dhorse-agent-"+ componentConstants.getVersion() +".jar";
		}
		if(!new File(javaAgentPath).exists()) {
			logger.error("The agent file does not exist, end to build dhorse agent image");
			return;
		}
		try {
			RegistryImage registryImage = RegistryImage.named(fullNameOfDHorseAgentImage(imageRepo)).addCredential(
					imageRepo.getAuthName(),
					imageRepo.getAuthPassword());
			Jib.from(Constants.BUSYBOX_IMAGE_URL)
				.addLayer(Arrays.asList(Paths.get(javaAgentPath)), AbsoluteUnixPath.get(Constants.USR_LOCAL_HOME))
				.containerize(Containerizer.to(registryImage)
						.setAllowInsecureRegistries(true)
						.addEventHandler(LogEvent.class, logEvent -> logger.info(logEvent.getMessage())));
		} catch (Exception e) {
			logger.error("Failed to build dhorse agent image", e);
		}
		logger.info("End to build dhorse agent image");
	}
	
	protected void downloadGradle() {
		String gradlePathName = componentConstants.getDataPath() + "gradle/";
		File gradlePath = new File(gradlePathName);
		if(!gradlePath.exists()) {
			if(gradlePath.mkdirs()) {
				logger.info("Create gradle path successfully");
			}else {
				logger.warn("Failed to create gradle path");
			}
		}
		
		String gradleHome = null;
		File[] files = gradlePath.listFiles();
		for(File f : files) {
			if(f.getName().equals(Constants.GRADLE_VERSION)) {
				gradleHome = f.getAbsolutePath();
				break;
			}
		}
		
		if(gradleHome != null) {
			logger.info("Gradle home is {}", gradleHome);
			return;
		}
		
		File targetFile = new File(gradlePathName + "gradle.zip");
		FileUtils.downloadFile(Constants.GRADLE_FILE_URL, targetFile);
		FileUtils.unZip(targetFile.getAbsolutePath(), gradlePathName);
		targetFile.delete();
	}
	
	protected void downloadMaven() {
		String rootPath = componentConstants.getDataPath() + "maven/";
		File rootPathFile = new File(rootPath);
		if(!rootPathFile.exists()) {
			if(rootPathFile.mkdirs()) {
				logger.info("Create maven path successfully");
			}else {
				logger.warn("Failed to create maven path");
			}
		}
		
		String mavenHome = null;
		File[] files = rootPathFile.listFiles();
		for(File f : files) {
			if(f.getName().equals(Constants.MAVEN_VERSION)) {
				mavenHome = f.getAbsolutePath();
				break;
			}
		}
		
		if(mavenHome != null) {
			logger.info("Maven home is {}", mavenHome);
			return;
		}
		
		File targetFile = new File(rootPath + "maven.zip");
		FileUtils.downloadFile(Constants.MAVEN_FILE_URL, targetFile);
		FileUtils.unTarGz(targetFile.getAbsolutePath(), rootPath);
		targetFile.delete();
	}
	
	protected void jibProperty() {
		System.setProperty("jib.httpTimeout", "600000");
		System.setProperty("sendCredentialsOverHttp", "true");
	}
	
	protected <D> PageData<D> zeroPageData(int pageSize) {
		PageData<D> pageData = new PageData<>();
		pageData.setPageNum(1);
		pageData.setPageCount(0);
		pageData.setPageSize(pageSize);
		pageData.setItemCount(0);
		pageData.setItems(null);
		return pageData;
	}
	
	protected <D> PageData<D> pageData(IPage<? extends BasePO> pagePO, List<D> items) {
		PageData<D> pageData = new PageData<>();
		pageData.setPageNum((int)pagePO.getCurrent());
		pageData.setPageCount((int)pagePO.getPages());
		pageData.setPageSize((int)pagePO.getSize());
		pageData.setItemCount((int)pagePO.getTotal());
		pageData.setItems(items);
		return pageData;
	}
}
