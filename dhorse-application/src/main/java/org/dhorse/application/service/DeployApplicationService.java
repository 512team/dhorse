package org.dhorse.application.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.maven.cli.DefaultCliRequest;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.dhorse.api.enums.BuildStatusEnum;
import org.dhorse.api.enums.CodeRepoTypeEnum;
import org.dhorse.api.enums.DeploymentStatusEnum;
import org.dhorse.api.enums.EnvExtTypeEnum;
import org.dhorse.api.enums.EventTypeEnum;
import org.dhorse.api.enums.ImageSourceEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.PackageBuildTypeEnum;
import org.dhorse.api.enums.PackageFileTypeEnum;
import org.dhorse.api.enums.TechTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.event.BuildMessage;
import org.dhorse.api.event.DeploymentMessage;
import org.dhorse.api.param.app.branch.BuildParam;
import org.dhorse.api.param.app.branch.deploy.AbortDeploymentParam;
import org.dhorse.api.param.app.branch.deploy.AbortDeploymentThreadParam;
import org.dhorse.api.response.EventResponse;
import org.dhorse.api.vo.App;
import org.dhorse.api.vo.AppExtendJava;
import org.dhorse.api.vo.AppExtendNode;
import org.dhorse.api.vo.DeploymentDetail;
import org.dhorse.api.vo.EnvHealth;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.GlobalConfigAgg.Maven;
import org.dhorse.infrastructure.param.AffinityTolerationParam;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.param.DeployParam;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.param.EnvExtParam;
import org.dhorse.infrastructure.repository.po.AffinityTolerationPO;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentDetailPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.strategy.repo.CodeRepoStrategy;
import org.dhorse.infrastructure.strategy.repo.GitHubCodeRepoStrategy;
import org.dhorse.infrastructure.strategy.repo.GitLabCodeRepoStrategy;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DeployContext;
import org.dhorse.infrastructure.utils.DeploymentThreadPoolUtils;
import org.dhorse.infrastructure.utils.HttpUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.Jib;
import com.google.cloud.tools.jib.api.LogEvent;
import com.google.cloud.tools.jib.api.RegistryImage;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;

/**
 * 
 * 部署基础应用服务
 * 
 * @author 天地之怪
 */
public abstract class DeployApplicationService extends ApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(DeployApplicationService.class);
	
	private static final String MAVEN_REPOSITORY_ID = "customized-nexus";
	
	private static final String MAVEN_REPOSITORY_URL = "https://repo.maven.apache.org/maven2";
	
    //private static Map<String, Thread> threadMap = new ConcurrentHashMap<>();
	
	@Value("${server.port}")
	private Integer serverPort;

	protected String buildVersion(BuildParam buildParam) {

		DeployContext context = buildVersionContext(buildParam);
		
		//异步构建
		ThreadPoolUtils.buildVersion(() -> {
			
			Integer status = BuildStatusEnum.BUILDED_SUCCESS.getCode();
			ThreadLocalUtils.putDeployContext(context);
			
			try {
				logger.info("Start to build version");

				// 2.下载分支代码
				if (context.getCodeRepoStrategy().downloadCode(context)) {
					logger.info("Download branch successfully");
				} else {
					LogUtils.throwException(logger, MessageCodeEnum.DOWNLOAD_BRANCH);
				}

				// 3.打包
				if (pack(context)) {
					logger.info("Pack successfully");
				} else {
					LogUtils.throwException(logger, MessageCodeEnum.PACK);
				}

				// 4.制作镜像并上传仓库
				if(buildImage(context)) {
					logger.info("Build image successfully");
				}else {
					LogUtils.throwException(logger, MessageCodeEnum.BUILD_IMAGE);
				}
				
				updateDeploymentVersionStatus(context.getId(), status);
			} catch (Throwable e) {
				status = BuildStatusEnum.BUILDED_FAILUR.getCode();
				updateDeploymentVersionStatus(context.getId(), status);
				logger.error("Failed to build version", e);
			} finally {
				buildNotify(context, status);
				logger.info("End to build version");
				ThreadLocalUtils.removeDeployContext();
			}
		});
		
		return context.getVersionName();
	}
	
	private void buildNotify(DeployContext context, int status) {
		if(context.getGlobalConfigAgg().getMore() == null) {
			return;
		}
		String url = context.getGlobalConfigAgg().getMore().getEventNotifyUrl();
		if(StringUtils.isBlank(url)){
			return;
		}
		BuildMessage message = new BuildMessage();
		message.setAppName(context.getApp().getAppName());
		message.setBranchName(context.getBranchName());
		message.setSubmitter(context.getSubmitter());
		message.setStatus(status);
		//todo
		//message.setTagName(context.get);
		message.setVerionName(context.getVersionName());
		
		EventResponse<BuildMessage> response = new EventResponse<>();
		response.setEventCode(EventTypeEnum.BUILD_VERSION.getCode());
		response.setData(message);
		doNotify(url, JsonUtils.toJsonString(response));
	}
	
	private void updateDeploymentVersionStatus(String id, Integer status) {
		DeploymentVersionParam deploymentVersionParam = new DeploymentVersionParam();
		deploymentVersionParam.setId(id);
		deploymentVersionParam.setStatus(status);
		deploymentVersionRepository.updateById(deploymentVersionParam);
	}
	
	protected boolean deploy(DeployParam deployParam) {
		//1.准备数据
		DeployContext context = checkAndBuildDeployContext(deployParam, DeploymentStatusEnum.DEPLOYING);
		
		//2.部署
		ThreadPoolUtils.deploy(() ->{
			
			Thread t = Thread.currentThread();
			DeploymentThreadPoolUtils.put(t.getName(), t);
			
			DeploymentDetailParam detailParam = new DeploymentDetailParam();
			detailParam.setId(deployParam.getDeploymentDetailId());
			try {
				detailParam.setDeploymentThread(InetAddress.getLocalHost().getHostAddress() + ":" + t.getName());
			} catch (UnknownHostException e) {
				logger.error("Failed to get host ip");
			}
			deploymentDetailRepository.update(detailParam);
			
			try {
				doDeploy(context);
			}finally {
				DeploymentThreadPoolUtils.remove(t.getName());
            }
		});
		
		return true;
	}
	
	private boolean doDeploy(DeployContext context) {

		ThreadLocalUtils.putDeployContext(context);
		DeploymentStatusEnum deploymentStatus = DeploymentStatusEnum.DEPLOYED_FAILURE;
		
		if(DeploymentThreadPoolUtils.isInterrupted()) {
			return false;
		}
		
		try {
			logger.info("Start to deploy env");
			
			// 1.部署
			if (context.getClusterStrategy().createDeployment(context)) {
				logger.info("Deploy successfully");
				deploymentStatus = DeploymentStatusEnum.DEPLOYED_SUCCESS;
				
				// 2.合并分支
				try {
					if (YesOrNoEnum.YES.getCode().equals(context.getAppEnv().getRequiredMerge())
							&& !"master".equals(context.getBranchName())
							&& !"main".equals(context.getBranchName())) {
						context.getCodeRepoStrategy().mergeBranch(context);
						deploymentStatus = DeploymentStatusEnum.MERGED_SUCCESS;
					}
				}catch (Exception e) {
					deploymentStatus = DeploymentStatusEnum.MERGED_FAILURE;
					logger.error("Failed to merge branch,", e);
				}
			} else {
				deploymentStatus = DeploymentStatusEnum.DEPLOYED_FAILURE;
			}
		} catch (Throwable e) {
			logger.error("Failed to deploy", e);
		} finally {
			updateDeployStatus(context, deploymentStatus, null, new Date());
			deployNotify(context, deploymentStatus);
			logger.info("End to deploy");
			ThreadLocalUtils.removeDeployContext();
		}

		return true;
	}

	private void deployNotify(DeployContext context, DeploymentStatusEnum status) {
		if(context.getGlobalConfigAgg().getMore() == null) {
			return;
		}
		String url = context.getGlobalConfigAgg().getMore().getEventNotifyUrl();
		if(StringUtils.isBlank(url)){
			return;
		}
		DeploymentMessage message = new DeploymentMessage();
		message.setEnvTag(context.getAppEnv().getTag());
		message.setAppName(context.getApp().getAppName());
		message.setBranchName(context.getBranchName());
		message.setSubmitter(context.getSubmitter());
		message.setApprover(context.getApprover());
		message.setStatus(status.getCode());
		//todo
		//message.setTagName(context.get);
		message.setVerionName(context.getVersionName());
		
		EventResponse<DeploymentMessage> response = new EventResponse<>();
		response.setEventCode(EventTypeEnum.DEPLOY_ENV.getCode());
		response.setData(message);
		doNotify(url, JsonUtils.toJsonString(response));
	}
	
	protected boolean rollback(DeployParam deployParam) {
		// 1.准备数据
		DeployContext context = checkAndBuildDeployContext(deployParam, DeploymentStatusEnum.ROLLBACKING);
		
		//2.回滚
		ThreadPoolUtils.deploy(() ->{
			doRollback(context);
		});
		
		return true;
	}
	
	private boolean doRollback(DeployContext context) {
		
		ThreadLocalUtils.putDeployContext(context);
		DeploymentStatusEnum rollbackStatus = DeploymentStatusEnum.ROLLBACK_FAILURE;
		
		try {
			logger.info("Start to rollback");
			// 部署
			if (context.getClusterStrategy().createDeployment(context)) {
				rollbackStatus = DeploymentStatusEnum.ROLLBACK_SUCCESS;
				logger.info("rollback successfully");
			} else {
				LogUtils.throwException(logger, MessageCodeEnum.DEPLOY);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			updateDeployStatus(context, rollbackStatus, null, new Date());
			logger.info("End to rollback");
			ThreadLocalUtils.removeDeployContext();
		}

		return true;
	}
	
	private DeployContext buildVersionContext(BuildParam buildParam) {
		GlobalConfigAgg globalConfig = globalConfig();
		App app = appRepository.queryWithExtendById(buildParam.getAppId());
		DeployContext context = new DeployContext();
		context.setSubmitter(buildParam.getSubmitter());
		context.setGlobalConfigAgg(globalConfig);
		context.setBranchName(buildParam.getBranchName());
		context.setApp(app);
		context.setComponentConstants(componentConstants);
		context.setCodeRepoStrategy(buildCodeRepo(context.getGlobalConfigAgg().getCodeRepo().getType()));
		
		//构建版本编号
		String nameOfImage = new StringBuilder()
				.append(context.getApp().getAppName())
				.append(":v")
				.append(new SimpleDateFormat(Constants.DATE_FORMAT_YYYYMMDDHHMMSS).format(new Date()))
				.toString();
		String fullNameOfImage = fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(), nameOfImage);
		context.setVersionName(nameOfImage);
		context.setFullNameOfImage(fullNameOfImage);
		
		//同一个应用，不允许同时构建多个版本
		DeploymentVersionParam bizParam = new DeploymentVersionParam();
		bizParam.setStatus(BuildStatusEnum.BUILDING.getCode());
		bizParam.setAppId(buildParam.getAppId());
		DeploymentVersionPO deploymentVersionPO = deploymentVersionRepository.query(bizParam);
		if(deploymentVersionPO != null) {
			LogUtils.throwException(logger, MessageCodeEnum.VERSION_IS_BUILDING);
		}
		
		//新增版本记录
		bizParam = new DeploymentVersionParam();
		bizParam.setBranchName(buildParam.getBranchName());
		bizParam.setVersionName(context.getVersionName());
		bizParam.setAppId(buildParam.getAppId());
		Date now = new Date();
		bizParam.setCreationTime(now);
		String id = deploymentVersionRepository.add(bizParam);
		context.setId(id);
		String logFilePath = Constants.buildVersionLogFile(context.getComponentConstants().getLogPath(),
				now, id);
		context.setLogFilePath(logFilePath);
		context.setEventType(EventTypeEnum.BUILD_VERSION);
		
		return context;
	}

	private DeployContext buildDeployContext(DeployParam deployParam) {
		GlobalConfigAgg globalConfig = globalConfig();
		AppEnvPO appEnvPO = appEnvRepository.queryById(deployParam.getEnvId());
		EnvExtParam bizParam = new EnvExtParam();
		bizParam.setExType(EnvExtTypeEnum.HEALTH.getCode());
		EnvHealth envHealth = envExtRepository.listEnvHealth();
		App app = appRepository.queryWithExtendById(appEnvPO.getAppId());
		AffinityTolerationParam affinityTolerationParam = new AffinityTolerationParam();
		affinityTolerationParam.setEnvId(appEnvPO.getId());
		affinityTolerationParam.setAppId(appEnvPO.getAppId());
		affinityTolerationParam.setOpenStatus(YesOrNoEnum.YES.getCode());
		List<AffinityTolerationPO> affinitys = affinityTolerationRepository.list(affinityTolerationParam);
		ClusterPO clusterPO = clusterRepository.queryById(appEnvPO.getClusterId());
		if(clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_EXISTENCE);
		}
		DeployContext context = new DeployContext();
		context.setSubmitter(deployParam.getDeployer());
		context.setApprover(deployParam.getApprover());
		context.setGlobalConfigAgg(globalConfig);
		context.setCodeRepoStrategy(buildCodeRepo(context.getGlobalConfigAgg().getCodeRepo().getType()));
		context.setCluster(clusterPO);
		context.setBranchName(deployParam.getBranchName());
		context.setApp(app);
		context.setAppEnv(appEnvPO);
		context.setEnvHealth(envHealth);
		context.setAffinitys(affinitys);
		context.setComponentConstants(componentConstants);
		context.setClusterStrategy(clusterStrategy(context.getCluster().getClusterType()));
		context.setId(deployParam.getDeploymentDetailId());
		context.setStartTime(deployParam.getDeploymentStartTime());
		context.setDeploymentName(K8sUtils.getDeploymentName(app.getAppName(), appEnvPO.getTag()));
		context.setVersionName(deployParam.getVersionName());
		String fullNameOfImage = fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(), deployParam.getVersionName());
		context.setFullNameOfImage(fullNameOfImage);
		context.setFullNameOfAgentImage(fullNameOfAgentImage(context));
		String logFilePath = Constants.deploymentLogFile(context.getComponentConstants().getLogPath(),
				context.getStartTime(), context.getId());
		context.setLogFilePath(logFilePath);
		context.setEventType(EventTypeEnum.DEPLOY_ENV);
		return context;
	}
	
	private DeployContext checkAndBuildDeployContext(DeployParam deployParam, DeploymentStatusEnum deploymentStatus) {
		DeployContext context = buildDeployContext(deployParam);
		// 当前环境是否存在部署中
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setEnvId(deployParam.getEnvId());
		deploymentDetailParam.setDeploymentStatuss(Arrays.asList(DeploymentStatusEnum.DEPLOYING.getCode(),
				DeploymentStatusEnum.ROLLBACKING.getCode()));
		DeploymentDetailPO deploymentDetailPO = deploymentDetailRepository.query(deploymentDetailParam);
		if (deploymentDetailPO != null) {
			LogUtils.throwException(logger, MessageCodeEnum.ENV_DEPLOYING);
		}
		updateDeployStatus(context, deploymentStatus, context.getStartTime(), null);
		return context;
	}

	private boolean pack(DeployContext context) {
		//SpringBoot应用
		if (TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())) {
			AppExtendJava appExtend = context.getApp().getAppExtend();
			if(PackageBuildTypeEnum.MAVEN.getCode().equals(appExtend.getPackageBuildType())) {
				return doMavenPack(context.getGlobalConfigAgg().getMaven(), context.getLocalPathOfBranch());
			}
		}
		//Node应用
		if (TechTypeEnum.NODE.getCode().equals(context.getApp().getTechType())) {
			AppExtendNode appExtend =  context.getApp().getAppExtend();
			Resource resource = new PathMatchingResourcePatternResolver()
					.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "maven/app_node_pom.xml");
			try (InputStream in = resource.getInputStream();
					FileOutputStream out = new FileOutputStream(context.getLocalPathOfBranch() + "pom.xml")) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				String lines = new String(buffer, "UTF-8");
				String result = lines.replace("${nodeVersion}", appExtend.getNodeVersion())
					.replace("${npmVersion}", appExtend.getNpmVersion().substring(1))
					.replace("${installDirectory}", mavenRepo() + "node/" + appExtend.getNodeVersion());
				out.write(result.toString().getBytes("UTF-8"));
			} catch (IOException e) {
				logger.error("Failed to build tmp app", e);
			}
			return doMavenPack(context.getGlobalConfigAgg().getMaven(), context.getLocalPathOfBranch());
		}
		
		return true;
	}

	protected CodeRepoStrategy buildCodeRepo(String codeRepoType) {
		if (CodeRepoTypeEnum.GITLAB.getValue().equals(codeRepoType)) {
			return new GitLabCodeRepoStrategy();
		} else {
			return new GitHubCodeRepoStrategy();
		}
	}

	public boolean doMavenPack(Maven mavenConf, String localPathOfBranch) {
		logger.info("Start to pack using maven");
		
		String localRepoPath = mavenRepo();
		System.setProperty(MavenCli.LOCAL_REPO_PROPERTY, localRepoPath);
		System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, localRepoPath);
		
		//首先使用指定的javaHome
		String javaHome = null;
		if(mavenConf != null && !StringUtils.isBlank(mavenConf.getJavaHome())) {
			javaHome = mavenConf.getJavaHome();
		}else {
			javaHome = System.getenv("JAVA_HOME");
		}
		
		if(javaHome == null) {
			LogUtils.throwException(logger, MessageCodeEnum.JAVA_HOME_IS_EMPTY);
		}
		
		String javaVersion = queryJavaMajorVersion(mavenConf);
		if(javaVersion == null) {
			LogUtils.throwException(logger, MessageCodeEnum.JAVA_VERSION_IS_EMPTY);
		}
		
		logger.info("Java home is {}", javaHome);
		
		logger.info("Java version is {}", javaVersion);
		
		String[] commands = new String[] {"clean", "package", "-Dmaven.test.skip"};
		DefaultCliRequest request = new DefaultCliRequest(commands, null);
		request.setWorkingDirectory(localPathOfBranch);
		
		Repository repository = new Repository();
		repository.setId("nexus");
		repository.setName("nexus");
		repository.setUrl(mavenConf != null && StringUtils.isNotBlank(mavenConf.getMavenRepoUrl())
				? mavenConf.getMavenRepoUrl() : MAVEN_REPOSITORY_URL);
		
		RepositoryPolicy policy = new RepositoryPolicy();
		policy.setEnabled(true);
		policy.setUpdatePolicy("always");
		policy.setChecksumPolicy("fail");
		
		repository.setReleases(policy);
		repository.setSnapshots(policy);
		
		Profile profile = new Profile();
		profile.setId(MAVEN_REPOSITORY_ID);
		Activation activation = new Activation();
		activation.setActiveByDefault(true);
		activation.setJdk(javaVersion);
		profile.setActivation(activation);
		profile.setRepositories(Arrays.asList(repository));
		profile.setPluginRepositories(Arrays.asList(repository));
		
		Properties properties = new Properties();
		properties.put("java.home", javaHome);
		properties.put("java.version", javaVersion);
		properties.put("maven.compiler.source", javaVersion);
		properties.put("maven.compiler.target", javaVersion);
		properties.put("maven.compiler.compilerVersion", javaVersion);
		properties.put("project.build.sourceEncoding", "UTF-8");
		properties.put("project.reporting.outputEncoding", "UTF-8");
		profile.setProperties(properties);
		MavenExecutionRequest executionRequest = request.getRequest();
		executionRequest.setProfiles(Arrays.asList(profile));
		executionRequest.setLoggingLevel(MavenExecutionRequest.LOGGING_LEVEL_INFO);
		
		MavenCli cli = new MavenCli();
		int status = 0;
		try {
			status = cli.doMain(request);
		} catch (Exception e) {
			logger.error("Failed to maven pack", e);
			return false;
		}
		return status == 0;
	}

	private boolean buildImage(DeployContext context) {
		if(TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())){
			return buildSpringBootImage(context);
		}
		
		if(TechTypeEnum.NODE.getCode().equals(context.getApp().getTechType())){
			return buildNodeImage(context);
		}
		
		return false;
	}
	
	private boolean buildSpringBootImage(DeployContext context) {
		AppExtendJava appExtend = context.getApp().getAppExtend();
		String fullTargetPath = context.getLocalPathOfBranch();
		if(StringUtils.isBlank(appExtend.getPackageTargetPath())) {
			fullTargetPath += "target/";
		}else {
			fullTargetPath += appExtend.getPackageTargetPath();
		}
		File packageTargetPath = Paths.get(fullTargetPath).toFile();
		if (!packageTargetPath.exists()) {
			logger.error("The target path does not exist");
			return false;
		}

		List<Path> targetFiles = new ArrayList<>();
		for (File file : packageTargetPath.listFiles()) {
			String packageFileType = PackageFileTypeEnum.getByCode(appExtend.getPackageFileType()).getValue();
			if (file.getName().endsWith("." + packageFileType)) {
				File targetFile = new File(file.getParent() + "/" + context.getApp().getAppName() + "." + packageFileType);
				file.renameTo(targetFile);
				targetFiles.add(targetFile.toPath());
				break;
			}
		}

		if (targetFiles.size() == 0) {
			logger.error("The target file does not exist");
			return false;
		} else if (targetFiles.size() > 1) {
			logger.error("Multiple target files exist");
			return false;
		}

		//基础镜像
		String baseImage = baseImage(context);
		String fileNameWithExtension = targetFiles.get(0).toFile().getName();
		List<String> entrypoint = Arrays.asList("java", "-jar", fileNameWithExtension);
		doBuildImage(context, baseImage, entrypoint, targetFiles);

		return true;
	}
	
	private boolean buildNodeImage(DeployContext context) {
		AppExtendNode appExtend = context.getApp().getAppExtend();
		String fullDistPath = context.getLocalPathOfBranch();
		if(StringUtils.isBlank(appExtend.getPackageTargetPath())) {
			fullDistPath += "dist/";
		}else {
			fullDistPath += appExtend.getPackageTargetPath();
		}
		File fullDistPathFile = new File(fullDistPath);
		if (!fullDistPathFile.exists()) {
			logger.error("The target path does not exist");
			return false;
		}
		
		//创建app的编译文件
		File appPathFile = new File(context.getLocalPathOfBranch() + context.getApp().getAppName());
		appPathFile.mkdirs();
		try {
			FileUtils.copyDirectory(fullDistPathFile, appPathFile);
		} catch (IOException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.COPY_FILE_FAILURE);
		}
		
		doBuildImage(context, "busybox:latest", null, Arrays.asList(appPathFile.toPath()));

		return true;
	}

	private void doBuildImage(DeployContext context, String baseImageName, List<String> entrypoint, List<Path> targetFiles) {
		String imageUrl = context.getGlobalConfigAgg().getImageRepo().getUrl();
		String imageServer = imageUrl.substring(imageUrl.indexOf("//") + 2);
		
		//设置连接仓库的超时时间
		System.setProperty("jib.httpTimeout", "15000");
		System.setProperty("sendCredentialsOverHttp", "true");
		try {
			RegistryImage baseImage = RegistryImage.named(baseImageName);
			if(baseImageName.startsWith(imageServer)) {
				baseImage.addCredential(
						context.getGlobalConfigAgg().getImageRepo().getAuthName(),
						context.getGlobalConfigAgg().getImageRepo().getAuthPassword());
			}
			
			RegistryImage registryImage = RegistryImage.named(context.getFullNameOfImage()).addCredential(
					context.getGlobalConfigAgg().getImageRepo().getAuthName(),
					context.getGlobalConfigAgg().getImageRepo().getAuthPassword());
			Jib.from(baseImage)
				.addLayer(targetFiles, AbsoluteUnixPath.get(Constants.CONTAINER_WORK_HOME))
				.setEntrypoint(entrypoint)
				//对于由alpine构建的镜像，使用addVolume(AbsoluteUnixPath.fromPath(Paths.get("/etc/localtime")))代码时时区才会生效。
				//但是，由于Jib不支持RUN命令，因此像RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime也无法使用，
				//不过，可以通过手动构建基础镜像来使用RUN，然后目标镜像再依赖基础镜像。
				.addEnvironmentVariable("TZ", "Asia/Shanghai")
				.containerize(Containerizer.to(registryImage)
						.setAllowInsecureRegistries(true)
						.addEventHandler(LogEvent.class, logEvent -> logger.info(logEvent.getMessage())));
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.BUILD_IMAGE);
		}
	}
	
	private String baseImage(DeployContext context) {
		String baseImage = context.getApp().getBaseImage();
		if (!TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())) {
			return baseImage;
		}
		AppExtendJava extend = (AppExtendJava)context.getApp().getAppExtend();
		//Jar类型文件的基础镜像都是Jdk镜像
		if(PackageFileTypeEnum.JAR.getCode().equals(extend.getPackageFileType())) {
			if(ImageSourceEnum.VERSION.getCode().equals(context.getApp().getBaseImageSource())) {
				return fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(),
						imageNameOfJdk(context.getApp().getBaseImageVersion()));
			}
			if(ImageSourceEnum.CUSTOM.getCode().equals(context.getApp().getBaseImageSource())) {
				return baseImage;
			}
		}
		//War类型文件的基础镜像都是busybox镜像
		if(PackageFileTypeEnum.WAR.getCode().equals(extend.getPackageFileType())) {
			baseImage = "busybox:latest";
		}
		return baseImage;
	}
	
	private boolean updateDeployStatus(DeployContext context, DeploymentStatusEnum status, Date startTime, Date endTime) {
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setId(context.getId());
		deploymentDetailParam.setDeploymentStatus(status.getCode());
		if(startTime != null) {
			deploymentDetailParam.setStartTime(startTime);
		}
		if(endTime != null) {
			deploymentDetailParam.setEndTime(endTime);
			AppEnvParam appEnvParam = new AppEnvParam();
			appEnvParam.setId(context.getAppEnv().getId());
			appEnvParam.setDeploymentTime(endTime);
			appEnvRepository.updateById(appEnvParam);
		}
		return deploymentDetailRepository.updateById(deploymentDetailParam);
	}
	
	public Void abortDeployment(LoginUser loginUser, AbortDeploymentParam abortParam) {
		DeploymentDetailParam bizParam = new DeploymentDetailParam();
		bizParam.setAppId(abortParam.getAppId());
		bizParam.setId(abortParam.getDeploymentDetailId());
		DeploymentDetail deploymentDetail = deploymentDetailRepository.query(loginUser, bizParam);
		if(deploymentDetail == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		if(!DeploymentStatusEnum.DEPLOYING.getCode().equals(deploymentDetail.getDeploymentStatus())
				&& !DeploymentStatusEnum.ROLLBACKING.getCode().equals(deploymentDetail.getDeploymentStatus())) {
			return null;
		}
		String thread = deploymentDetail.getDeploymentThread();
		if(StringUtils.isBlank(thread)) {
			return null;
		}
		
		AbortDeploymentThreadParam threadParam = new AbortDeploymentThreadParam();
		threadParam.setAppId(abortParam.getAppId());
		threadParam.setDeploymentDetailId(abortParam.getDeploymentDetailId());
		threadParam.setThreadName(thread.split(":")[1]);
		
		Map<String, String> cookieParam = Collections.singletonMap("login_token", loginUser.getLastLoginToken());
		
		String url = "http://" + thread.split(":")[0] + ":" + serverPort + "/app/deployment/detail/abortDeploymentThread";
		try(CloseableHttpResponse httResponse = HttpUtils.post(url, JsonUtils.toJsonString(threadParam), cookieParam)){
			int httpCode = httResponse.getStatusLine().getStatusCode();
			logger.info("url code: {}", httpCode);
		}catch (Exception e) {
			logger.error("Failed to abortDeploymentThread, url: " + url, e);
		}
		
		return null;
	}
	
	public Void abortDeploymentThread(LoginUser loginUser, AbortDeploymentThreadParam abortParam) {
		DeploymentDetailParam bizParam = new DeploymentDetailParam();
		bizParam.setAppId(abortParam.getAppId());
		bizParam.setId(abortParam.getDeploymentDetailId());
		DeploymentDetail deploymentDetail = deploymentDetailRepository.query(loginUser, bizParam);
		if(deploymentDetail == null) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}
		if(!DeploymentStatusEnum.DEPLOYING.getCode().equals(deploymentDetail.getDeploymentStatus())
				&& !DeploymentStatusEnum.ROLLBACKING.getCode().equals(deploymentDetail.getDeploymentStatus())) {
			return null;
		}
		String thread = deploymentDetail.getDeploymentThread();
		if(StringUtils.isBlank(thread)) {
			return null;
		}
		DeploymentThreadPoolUtils.interrupt(abortParam.getThreadName());
		if(DeploymentStatusEnum.DEPLOYING.getCode().equals(deploymentDetail.getDeploymentStatus())) {
			bizParam.setDeploymentStatus(DeploymentStatusEnum.DEPLOYED_FAILURE.getCode());
		}else if(DeploymentStatusEnum.ROLLBACKING.getCode().equals(deploymentDetail.getDeploymentStatus())) {
			bizParam.setDeploymentStatus(DeploymentStatusEnum.ROLLBACK_FAILURE.getCode());
		}
		deploymentDetailRepository.updateById(bizParam);
		return null;
	}
}