package org.dhorse.application.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.cli.DefaultCliRequest;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.dhorse.api.enums.CodeRepoTypeEnum;
import org.dhorse.api.enums.DeploymentStatusEnum;
import org.dhorse.api.enums.DeploymentVersionStatusEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.LanguageTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.PackageBuildTypeEnum;
import org.dhorse.api.enums.PackageFileTypeTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.project.branch.VersionBuildParam;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.vo.GlobalConfigAgg.Maven;
import org.dhorse.api.vo.Project;
import org.dhorse.api.vo.ProjectExtendJava;
import org.dhorse.infrastructure.param.DeployParam;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.param.ProjectEnvParam;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.repository.po.DeploymentDetailPO;
import org.dhorse.infrastructure.repository.po.DeploymentVersionPO;
import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.strategy.repo.CodeRepoStrategy;
import org.dhorse.infrastructure.strategy.repo.GitHubCodeRepoStrategy;
import org.dhorse.infrastructure.strategy.repo.GitLabCodeRepoStrategy;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DeployContext;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.Jib;
import com.google.cloud.tools.jib.api.JibContainerBuilder;
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

	protected String buildVersion(VersionBuildParam versionBuildParam) {

		DeployContext context = buildVersionContext(versionBuildParam);
		
		//异步构建
		ThreadPoolUtils.buildVersion(() -> {
			ThreadLocalUtils.putDeployContext(context);
			try {
				logger.info("Start to build version");

				// 2.下载分支代码
				if (context.getCodeRepoStrategy().downloadBranch(context)) {
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
				
				updateDeploymentVersionStatus(context.getId(), DeploymentVersionStatusEnum.BUILDED_SUCCESS.getCode());
			} catch (Throwable e) {
				updateDeploymentVersionStatus(context.getId(), DeploymentVersionStatusEnum.BUILDED_FAILUR.getCode());
				logger.error("Failed to build version", e);
			} finally {
				logger.info("End to build version");
				ThreadLocalUtils.removeDeployContext();
			}
		});
		
		return context.getNameOfImage();
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
			doDeploy(context);
		});
		
		return true;
	}
	
	private boolean doDeploy(DeployContext context) {

		ThreadLocalUtils.putDeployContext(context);
		DeploymentStatusEnum deploymentStatus = DeploymentStatusEnum.DEPLOYED_FAILURE;
		
		try {
			logger.info("Start to deploy env");
			
			// 1.部署
			if (context.getClusterStrategy().createDeployment(context)) {
				logger.info("Deploy successfully");
				deploymentStatus = DeploymentStatusEnum.DEPLOYED_SUCCESS;
				
				// 2.合并分支
				try {
					if (YesOrNoEnum.YES.getCode().equals(context.getProjectEnv().getRequiredMerge())
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
			logger.info("End to deploy");
			ThreadLocalUtils.removeDeployContext();
		}

		return true;
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
	
	private DeployContext buildVersionContext(VersionBuildParam versionBuildParam) {
		GlobalConfigAgg globalConfig = globalConfig();
		Project project = projectRepository.queryWithExtendById(versionBuildParam.getProjectId());
		DeployContext context = new DeployContext();
		context.setGlobalConfigAgg(globalConfig);
		context.setBranchName(versionBuildParam.getBranchName());
		context.setProject(project);
		context.setComponentConstants(componentConstants);
		context.setCodeRepoStrategy(buildCodeRepo(context.getGlobalConfigAgg().getCodeRepo().getType()));
		
		//构建版本编号
		String nameOfImage = new StringBuilder()
				.append(context.getProject().getProjectName())
				.append(":")
				.append(new SimpleDateFormat(Constants.DATE_FORMAT_YYYYMMDDHHMMSS).format(new Date()))
				.toString();
		String fullNameOfImage = fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(), nameOfImage);
		context.setNameOfImage(nameOfImage);
		context.setFullNameOfImage(fullNameOfImage);
		
		//同一个项目，不允许同时构建多个版本
		DeploymentVersionParam bizParam = new DeploymentVersionParam();
		bizParam.setStatus(DeploymentVersionStatusEnum.BUILDING.getCode());
		bizParam.setProjectId(versionBuildParam.getProjectId());
		DeploymentVersionPO deploymentVersionPO = deploymentVersionRepository.query(bizParam);
		if(deploymentVersionPO != null) {
			LogUtils.throwException(logger, MessageCodeEnum.VERSION_IS_BUILDING);
		}
		
		//新增版本记录
		bizParam = new DeploymentVersionParam();
		bizParam.setBranchName(versionBuildParam.getBranchName());
		bizParam.setVersionName(context.getNameOfImage());
		bizParam.setProjectId(versionBuildParam.getProjectId());
		Date now = new Date();
		bizParam.setCreationTime(now);
		String id = deploymentVersionRepository.add(bizParam);
		context.setId(id);
		String logFilePath = Constants.buildVersionLogFile(context.getComponentConstants().getLogPath(),
				now, id);
		context.setLogFilePath(logFilePath);
		
		return context;
	}

	private DeployContext buildDeployContext(DeployParam deployParam) {
		GlobalConfigAgg globalConfig = globalConfig();
		ProjectEnvPO projectEnvPO = projectEnvRepository.queryById(deployParam.getEnvId());
		Project project = projectRepository.queryWithExtendById(projectEnvPO.getProjectId());
		ClusterPO clusterPO = clusterRepository.queryById(projectEnvPO.getClusterId());
		if(clusterPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.CLUSER_EXISTENCE);
		}
		DeployContext context = new DeployContext();
		context.setGlobalConfigAgg(globalConfig);
		context.setCodeRepoStrategy(buildCodeRepo(context.getGlobalConfigAgg().getCodeRepo().getType()));
		context.setCluster(clusterPO);
		context.setBranchName(deployParam.getBranchName());
		context.setProject(project);
		context.setProjectEnv(projectEnvPO);
		context.setComponentConstants(componentConstants);
		context.setClusterStrategy(clusterStrategy(context.getCluster().getClusterType()));
		context.setId(deployParam.getDeploymentDetailId());
		context.setStartTime(deployParam.getDeploymentStartTime());
		context.setDeploymentAppName(K8sUtils.getReplicaAppName(project.getProjectName(), projectEnvPO.getTag()));
		context.setNameOfImage(deployParam.getVersionName());
		String fullNameOfImage = fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(), deployParam.getVersionName());
		context.setFullNameOfImage(fullNameOfImage);
		String logFilePath = Constants.deploymentLogFile(context.getComponentConstants().getLogPath(),
				context.getStartTime(), context.getId());
		context.setLogFilePath(logFilePath);
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
		if (!LanguageTypeEnum.JAVA.getCode().equals(context.getProject().getLanguageType())) {
			logger.info("No need to pack");
			return true;
		}
		ProjectExtendJava projectExtend = context.getProject().getProjectExtend();
		if (PackageBuildTypeEnum.MAVEN.getCode().equals(projectExtend.getPackageBuildType())) {
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
		
		String localRepoPath = componentConstants.getDataPath() + "repository";
		System.setProperty(MavenCli.LOCAL_REPO_PROPERTY, localRepoPath);
		System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, localRepoPath);
		
		//首先使用指定的javahome
		String javaHome = null;
		if(mavenConf != null && !StringUtils.isBlank(mavenConf.getJavaHome())) {
			javaHome = mavenConf.getJavaHome();
		}else {
			javaHome = System.getenv("JAVA_HOME");
		}
		
		if(javaHome == null) {
			LogUtils.throwException(logger, MessageCodeEnum.JAVA_HOME_IS_EMPTY);
		}
		
		String javaVersion = null;
		if(mavenConf != null && !StringUtils.isBlank(mavenConf.getJavaVersion())) {
			javaVersion = mavenConf.getJavaVersion();
		}else {
			javaVersion = System.getProperty("java.version");
		}
		
		if(javaVersion == null) {
			LogUtils.throwException(logger, MessageCodeEnum.JAVA_VERSION_IS_EMPTY);
		}
		
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

	public boolean buildImage(DeployContext context) {
		ProjectExtendJava projectExtend = context.getProject().getProjectExtend();
		String fullTargetPath = context.getLocalPathOfBranch();
		if(StringUtils.isBlank(projectExtend.getPackageTargetPath())) {
			fullTargetPath += "target/";
		}else {
			fullTargetPath += projectExtend.getPackageTargetPath();
		}
		File packageTargetPath = Paths.get(fullTargetPath).toFile();
		if (!packageTargetPath.exists()) {
			logger.error("The target path does not exsit");
			return false;
		}

		List<Path> targetFiles = new ArrayList<>();
		for (File file : packageTargetPath.listFiles()) {
			String packageFileType = PackageFileTypeTypeEnum.getByCode(projectExtend.getPackageFileType()).getValue();
			if (file.getName().endsWith("." + packageFileType)) {
				File targetFile = new File(file.getParent() + "/" + context.getProject().getProjectName() + "." + packageFileType);
				file.renameTo(targetFile);
				targetFiles.add(targetFile.toPath());
				break;
			}
		}

		if (targetFiles.size() == 0) {
			logger.error("The target file does not exsit");
			return false;
		} else if (targetFiles.size() > 1) {
			logger.error("Multiple target files exist");
			return false;
		}

		try {
			JibContainerBuilder jibContainerBuilder = null;
			if (StringUtils.isBlank(context.getProject().getBaseImage())) {
				jibContainerBuilder = Jib.fromScratch();
			} else {
				jibContainerBuilder = Jib.from(context.getProject().getBaseImage());
			}
			//连接镜像仓库5秒超时
			System.setProperty("jib.httpTimeout", "5000");
			System.setProperty("sendCredentialsOverHttp", "true");
			String fileNameWithExtension = targetFiles.get(0).toFile().getName();
			List<String> entrypoint = Arrays.asList("java", "-jar", fileNameWithExtension);
			RegistryImage registryImage = RegistryImage.named(context.getFullNameOfImage()).addCredential(
					context.getGlobalConfigAgg().getImageRepo().getAuthUser(),
					context.getGlobalConfigAgg().getImageRepo().getAuthPassword());
			jibContainerBuilder.addLayer(targetFiles, "/")
				.setEntrypoint(entrypoint)
				.addVolume(AbsoluteUnixPath.fromPath(Paths.get("/etc/localtime")))
				.containerize(Containerizer.to(registryImage)
						.setAllowInsecureRegistries(true)
						.addEventHandler(LogEvent.class, logEvent -> logger.info(logEvent.getMessage())));
		} catch (Exception e) {
			logger.error("Failed to build image", e);
			return false;
		}

		return true;
	}

	private String fullNameOfImage(ImageRepo imageRepo, String nameOfImage) {
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
			fullNameOfImage.append(imageRepo.getAuthUser());
		}else {
			fullNameOfImage.append(Constants.IMAGE_REPO_PROJECT);
		}
		fullNameOfImage.append("/").append(nameOfImage);
		return fullNameOfImage.toString();
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
			ProjectEnvParam projectEnvParam = new ProjectEnvParam();
			projectEnvParam.setId(context.getProjectEnv().getId());
			projectEnvParam.setDeploymentTime(endTime);
			projectEnvRepository.updateById(projectEnvParam);
		}
		return deploymentDetailRepository.updateById(deploymentDetailParam);
	}
}
