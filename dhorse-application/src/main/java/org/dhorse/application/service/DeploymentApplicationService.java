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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.dhorse.api.enums.BuildStatusEnum;
import org.dhorse.api.enums.CodeRepoTypeEnum;
import org.dhorse.api.enums.DeploymentStatusEnum;
import org.dhorse.api.enums.EnvExtTypeEnum;
import org.dhorse.api.enums.EventTypeEnum;
import org.dhorse.api.enums.ImageSourceEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.NodeCompileTypeEnum;
import org.dhorse.api.enums.PackageBuildTypeEnum;
import org.dhorse.api.enums.PackageFileTypeEnum;
import org.dhorse.api.enums.TechTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.event.BuildVersionMessage;
import org.dhorse.api.event.DeploymentMessage;
import org.dhorse.api.param.app.branch.BuildParam;
import org.dhorse.api.param.app.branch.deploy.AbortDeploymentParam;
import org.dhorse.api.param.app.branch.deploy.AbortDeploymentThreadParam;
import org.dhorse.api.response.EventResponse;
import org.dhorse.api.response.model.App;
import org.dhorse.api.response.model.AppEnv.EnvExtendNode;
import org.dhorse.api.response.model.AppEnv.EnvExtendSpringBoot;
import org.dhorse.api.response.model.AppExtendGo;
import org.dhorse.api.response.model.AppExtendJava;
import org.dhorse.api.response.model.AppExtendNode;
import org.dhorse.api.response.model.AppExtendNuxt;
import org.dhorse.api.response.model.AppExtendPython;
import org.dhorse.api.response.model.DeploymentDetail;
import org.dhorse.api.response.model.EnvHealth;
import org.dhorse.api.response.model.EnvLifecycle;
import org.dhorse.api.response.model.EnvPrometheus;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
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
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.dhorse.infrastructure.utils.DeploymentThreadPoolUtils;
import org.dhorse.infrastructure.utils.FileUtils;
import org.dhorse.infrastructure.utils.HttpUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.StringUtils;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
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
public abstract class DeploymentApplicationService extends ApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentApplicationService.class);

	private static final String MAVEN_REPOSITORY_URL = "https://maven.aliyun.com/nexus/content/groups/public";

	@Value("${server.port}")
	private Integer serverPort;

	protected String buildVersion(BuildParam buildParam) {

		DeploymentContext context = buildVersionContext(buildParam);

		//异步构建
		ThreadPoolUtils.buildVersion(() -> {

			Integer status = BuildStatusEnum.BUILDED_SUCCESS.getCode();
			ThreadLocalUtils.Deployment.put(context);

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
					LogUtils.throwException(logger, MessageCodeEnum.PACK_FAILURE);
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
				ThreadLocalUtils.Deployment.remove();
			}
		});

		return context.getVersionName();
	}

	private void buildNotify(DeploymentContext context, int status) {
		if(context.getGlobalConfigAgg().getMore() == null) {
			return;
		}
		String url = context.getGlobalConfigAgg().getMore().getEventNotifyUrl();
		if(StringUtils.isBlank(url)){
			return;
		}

		BuildVersionMessage message = new BuildVersionMessage();
		message.setAppName(context.getApp().getAppName());
		message.setBranchName(context.getBranchName());
		message.setSubmitter(context.getSubmitter());
		message.setStatus(status);
		message.setVerionName(context.getVersionName());

		EventResponse<BuildVersionMessage> response = new EventResponse<>();
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
		DeploymentContext context = checkAndBuildDeployContext(deployParam, DeploymentStatusEnum.DEPLOYING);

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

	private boolean doDeploy(DeploymentContext context) {

		ThreadLocalUtils.Deployment.put(context);
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
			ThreadLocalUtils.Deployment.remove();
		}

		return true;
	}

	private void deployNotify(DeploymentContext context, DeploymentStatusEnum status) {
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
		DeploymentContext context = checkAndBuildDeployContext(deployParam, DeploymentStatusEnum.ROLLBACKING);

		//2.回滚
		ThreadPoolUtils.deploy(() ->{
			doRollback(context);
		});

		return true;
	}

	private boolean doRollback(DeploymentContext context) {

		ThreadLocalUtils.Deployment.put(context);
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
			ThreadLocalUtils.Deployment.remove();
		}

		return true;
	}

	private DeploymentContext buildVersionContext(BuildParam buildParam) {
		GlobalConfigAgg globalConfig = globalConfig();
		App app = appRepository.queryWithExtendById(buildParam.getAppId());
		DeploymentContext context = new DeploymentContext();
		if(!StringUtils.isBlank(buildParam.getEnvId())) {
			AppEnvPO appEnvPO = appEnvRepository.queryById(buildParam.getEnvId());
			context.setAppEnv(appEnvPO);
		}

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
				.append(new SimpleDateFormat(Constants.DATE_FORMAT_YYYYMMDD_HHMMSS).format(new Date()))
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
		bizParam.setEnvId(buildParam.getEnvId());
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

	private DeploymentContext deploymentContext(DeployParam deployParam) {
		GlobalConfigAgg globalConfig = globalConfig();
		AppEnvPO appEnvPO = appEnvRepository.queryById(deployParam.getEnvId());
		EnvHealth envHealth = envHealth(deployParam);
		EnvLifecycle lifecycle = lifecycle(deployParam);
		EnvPrometheus prometheus = prometheus(deployParam);
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
		DeploymentContext context = new DeploymentContext();
		if(!StringUtils.isBlank(appEnvPO.getExt())) {
			if(TechTypeEnum.SPRING_BOOT.getCode().equals(app.getTechType())) {
				context.setEnvExtend(JsonUtils.parseToObject(appEnvPO.getExt(), EnvExtendSpringBoot.class));
			}else {
				context.setEnvExtend(JsonUtils.parseToObject(appEnvPO.getExt(), EnvExtendNode.class));
			}
		}

		context.setSubmitter(deployParam.getDeployer());
		context.setApprover(deployParam.getApprover());
		context.setGlobalConfigAgg(globalConfig);
		context.setCodeRepoStrategy(buildCodeRepo(context.getGlobalConfigAgg().getCodeRepo().getType()));
		context.setCluster(clusterPO);
		context.setBranchName(deployParam.getBranchName());
		context.setApp(app);
		context.setAppEnv(appEnvPO);
		context.setEnvHealth(envHealth);
		context.setEnvLifecycle(lifecycle);
		context.setEnvPrometheus(prometheus);
		context.setAffinitys(affinitys);
		context.setComponentConstants(componentConstants);
		context.setClusterStrategy(clusterStrategy(context.getCluster().getClusterType()));
		context.setId(deployParam.getDeploymentDetailId());
		context.setStartTime(deployParam.getDeploymentStartTime());
		context.setDeploymentName(K8sUtils.getDeploymentName(app.getAppName(), appEnvPO.getTag()));
		context.setVersionName(deployParam.getVersionName());
		String fullNameOfImage = fullNameOfImage(context.getGlobalConfigAgg().getImageRepo(), deployParam.getVersionName());
		context.setFullNameOfImage(fullNameOfImage);
		context.setFullNameOfTraceAgentImage(fullNameOfTraceAgentImage(context));
		context.setFullNameOfDHorseAgentImage(fullNameOfDHorseAgentImage(context.getGlobalConfigAgg().getImageRepo()));
		String logFilePath = Constants.deploymentLogFile(context.getComponentConstants().getLogPath(),
				context.getStartTime(), context.getId());
		context.setLogFilePath(logFilePath);
		context.setEventType(EventTypeEnum.DEPLOY_ENV);
		return context;
	}

	private EnvHealth envHealth(DeployParam deployParam) {
		EnvExtParam extParam = new EnvExtParam();
		extParam.setExType(EnvExtTypeEnum.HEALTH.getCode());
		extParam.setAppId(deployParam.getAppId());
		extParam.setEnvId(deployParam.getEnvId());
		EnvHealth envHealth = envExtRepository.listEnvHealth(extParam);
		return envHealth;
	}

	private EnvLifecycle lifecycle(DeployParam deployParam) {
		EnvExtParam extParam = new EnvExtParam();
		extParam.setExType(EnvExtTypeEnum.LIFECYCLE.getCode());
		extParam.setAppId(deployParam.getAppId());
		extParam.setEnvId(deployParam.getEnvId());
		EnvLifecycle lifecycle = envExtRepository.listLifecycle(extParam);
		return lifecycle;
	}

	private EnvPrometheus prometheus(DeployParam deployParam) {
		EnvExtParam extParam = new EnvExtParam();
		extParam.setExType(EnvExtTypeEnum.PROMETHEUS.getCode());
		extParam.setAppId(deployParam.getAppId());
		extParam.setEnvId(deployParam.getEnvId());
		EnvPrometheus model = envExtRepository.queryPrometheus(extParam);
		return model;
	}

	private DeploymentContext checkAndBuildDeployContext(DeployParam deployParam, DeploymentStatusEnum deploymentStatus) {
		DeploymentContext context = deploymentContext(deployParam);
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

	private boolean pack(DeploymentContext context) {
		//SpringBoot应用
		if (TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())) {
			AppExtendJava appExtend = context.getApp().getAppExtend();
			if(PackageBuildTypeEnum.MAVEN.getCode().equals(appExtend.getPackageBuildType())) {
				return packByMaven(context, appExtend.getJavaHome());
			}else if(PackageBuildTypeEnum.GRADLE.getCode().equals(appExtend.getPackageBuildType())){
				return packByGradle(context);
			}
		}
		// Node应用
		if (TechTypeEnum.VUE.getCode().equals(context.getApp().getTechType())
				|| TechTypeEnum.REACT.getCode().equals(context.getApp().getTechType())) {
			AppExtendNode appExtend =  context.getApp().getAppExtend();
			String pomName = "";
			if (Objects.isNull(appExtend.getCompileType())
					|| NodeCompileTypeEnum.NPM.getCode().equals(appExtend.getCompileType())) {
				pomName = "maven/app_node_pom.xml";
			} else if (NodeCompileTypeEnum.PNPM.getCode().equals(appExtend.getCompileType())) {
				pomName = "maven/app_node_pnpm_pom.xml";
			} else if (NodeCompileTypeEnum.YARN.getCode().equals(appExtend.getCompileType())) {
				pomName = "maven/app_node_yarn_pom.xml";
			}
			Resource resource = new PathMatchingResourcePatternResolver()
					.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + pomName);
			try (InputStream in = resource.getInputStream();
					FileOutputStream out = new FileOutputStream(context.getLocalPathOfBranch() + "pom.xml")) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				String lines = new String(buffer, "UTF-8");
				String envTag = "";
				if(context.getAppEnv() != null) {
					envTag = ":" + context.getAppEnv().getTag();
				}
				String result = lines.replace("${env}", envTag)
						.replace("${nodeVersion}", appExtend.getNodeVersion())
						.replace("${installDirectory}", mavenRepo() + "node/" + appExtend.getNodeVersion());
				if (Objects.isNull(appExtend.getCompileType())
						|| NodeCompileTypeEnum.NPM.getCode().equals(appExtend.getCompileType())) {
					result = result
							.replace("${npmVersion}", appExtend.getNpmVersion().substring(1));
				} else if (NodeCompileTypeEnum.PNPM.getCode().equals(appExtend.getCompileType())) {
					result = result
							.replace("${pnpmVersion}", appExtend.getPnpmVersion().substring(1));
				} else if (NodeCompileTypeEnum.YARN.getCode().equals(appExtend.getCompileType())) {
					result = result
							.replace("${yarnVersion}", appExtend.getYarnVersion());
				}
				out.write(result.toString().getBytes("UTF-8"));
			} catch (IOException e) {
				logger.error("Failed to build node app", e);
			}
			return packByMaven(context, null);
		}

		// Go应用
		if (TechTypeEnum.GO.getCode().equals(context.getApp().getTechType())) {
			return packByGo(context);
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

	public boolean packByMaven(DeploymentContext context, String customizedJavaHome) {
		logger.info("Start to pack by maven");

		downloadMaven();

		String localRepoPath = mavenRepo();
		String[] repoUrls = repoUrls(context);

		Resource resource = new PathMatchingResourcePatternResolver()
				.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "maven/settings.xml");
		try (InputStream in = resource.getInputStream();
				FileOutputStream out = new FileOutputStream(context.getLocalPathOfBranch() + "settings.xml")) {
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			String lines = new String(buffer, "UTF-8");
			String result = lines.replace("${localRepository}", localRepoPath)
					.replace("${repositoryUrl1}", repoUrls[0])
					.replace("${repositoryUrl2}", repoUrls[1]);
			out.write(result.toString().getBytes("UTF-8"));
		} catch (IOException e) {
			logger.error("Failed to build node app", e);
		}

		boolean isSuccess = doPackByMaven(context, componentConstants.getDataPath() + "maven/", customizedJavaHome);

		logger.info("End to pack by maven");

		return isSuccess;
	}

	private String[] repoUrls(DeploymentContext context) {
		String[] result = new String[] {MAVEN_REPOSITORY_URL, MAVEN_REPOSITORY_URL};
		if(context.getGlobalConfigAgg().getMaven() == null) {
			return result;
		}
		List<String> repoUrls = context.getGlobalConfigAgg().getMaven().getMavenRepoUrl();
		if(!CollectionUtils.isEmpty(repoUrls) && repoUrls.size() == 1) {
			result[0] = repoUrls.get(0);
		}else if(!CollectionUtils.isEmpty(repoUrls) && repoUrls.size() == 2) {
			result[0] = repoUrls.get(0);
			result[1] = repoUrls.get(1);
		}

		if(StringUtils.isBlank(result[0])) {
			result[0] = MAVEN_REPOSITORY_URL;
		}
		if(StringUtils.isBlank(result[1])) {
			result[1] = MAVEN_REPOSITORY_URL;
		}

		return result;
	}

	private boolean doPackByMaven(DeploymentContext context, String mavenHome, String customizedJavaHome) {
		//命令格式：
		//cd /opt/dhorse/data/app/hello\hello-1689587261061 && \
		//opt/dhorse/data/maven/apache-maven-3.9.3/bin/mvn clean package \
		//-s /opt/dhorse/data/app/hello/settings.xml
		String settingsFile = context.getLocalPathOfBranch() + "settings.xml";
		String mavenBin = mavenHome + Constants.MAVEN_VERSION + "/bin/mvn";
		StringBuilder cmd = new StringBuilder();
		if(!Constants.isWindows()) {
			cmd.append("chmod +x ").append(mavenBin).append(" && ");
		}
		cmd.append("cd " + context.getLocalPathOfBranch())
				.append(" && ").append(mavenBin)
				.append(" clean package -Dmaven.test.skip ")
				.append("-s " + settingsFile);

		return execCommand(customizedJavaHome, cmd.toString());
    }

	private boolean packByGradle(DeploymentContext context) {
		logger.info("Start to pack by gradle");

		downloadGradle();

		AppExtendJava appExtend = context.getApp().getAppExtend();
		boolean isSuccess = doPackByGradle(context, componentConstants.getDataPath() + "gradle/", appExtend.getJavaHome());

		logger.info("End to pack by gradle");
		return isSuccess;
	}

	private boolean doPackByGradle(DeploymentContext context, String gradleHome, String customizedJavaHome) {
		//命令格式：cd /opt/dhorse/data/app/hello-gradle/hello-gradle-1688370652223/ \
		//&& /opt/dhorse/data/gradle/gradle-8.1.1/bin/gradle \
		//clean build
		//-g /opt/dhorse/data/gradle/cache
		String gradleBin = gradleHome + Constants.GRADLE_VERSION + "/bin/gradle";
		StringBuilder cmd = new StringBuilder();
		if(!Constants.isWindows()) {
			cmd.append("chmod +x ").append(gradleBin).append(" && ");
		}
		cmd.append("cd " + context.getLocalPathOfBranch())
				.append(" && ").append(gradleBin)
				.append(" clean build")
				.append(" -g " + gradleHome + "cache");

		return execCommand(customizedJavaHome, cmd.toString());
    }

	private boolean execCommand(String customizedJavaHome, String cmd) {
        String javaHome = javaHome(customizedJavaHome);
        logger.info("Java home is {}", javaHome);
        Map<String, String> env = new HashMap<>();
        env.put("JAVA_HOME", javaHome);
        return execCommand(env, cmd);
    }

	public boolean packByGo(DeploymentContext context) {
		AppExtendGo appExtend = context.getApp().getAppExtend();
		//统一下载amd64安装文件，并允许交叉编译，即设置CGO_ENABLED=0
		String goHome = downloadGo(appExtend.getGoVersion().substring(1));
		String goPath = new File(goHome).getParent();
		String goBin = goHome + "bin/go";
		Map<String, String> env = new HashMap<>();
        env.put("CGO_ENABLED", "0");
        env.put("GOOS", "linux");
        env.put("GOARCH", "amd64");
        env.put("GOPROXY", "https://goproxy.cn");
        env.put("GOCACHE", goPath + "/cache/build");
        env.put("GOMODCACHE", goPath + "/cache/pkg/mod");
        env.put("GOPATH", goPath + "/cache");

        String appName = context.getApp().getAppName();
        StringBuilder cmd = new StringBuilder();
		if(!Constants.isWindows()) {
			cmd.append("chmod +x ").append(goBin).append(" && ");
			cmd.append("chmod +x ").append(goHome + "pkg/tool/linux_amd64/*").append(" && ");
		}
		//指令格式：
		//cd /opt/data/app/hello-go/hello-go-1693449686623 \
		//&& /opt/data/go/go-1.20.7/bin/go mod init hello-go \
		//&& /opt/data/go/go-1.20.7/bin/go mod tidy \
		//&& /opt/data/go/go-1.20.7/bin/go mod download \
		//&& /opt/data/go/go-1.20.7/bin/go build -o hello-go
		cmd.append("cd " + context.getLocalPathOfBranch())
				.append(" && " + goBin + " mod init " + appName)
				.append(" && " + goBin + " mod tidy")
				.append(" && " + goBin + " mod download")
				.append(" && " + goBin + " build -o " + appName);

		if(!execCommand(env, cmd.toString())) {
			logger.error("Failed to pack by go");
			return false;
		}

		return true;
	}

	private boolean buildImage(DeploymentContext context) {

		buildSpringBootImage(context);

		buildNodeImage(context);

		buildNodejsImage(context);
		
		buildNuxtImage(context);

		buildHtmlImage(context);

		buildGoImage(context);

		buildPythonImage(context);

		buildFlaskImage(context);

		buildDjangoImage(context);

		return true;
	}

	private void buildSpringBootImage(DeploymentContext context) {
		if(!TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())){
			return;
		}
		AppExtendJava appExtend = context.getApp().getAppExtend();
		String fullTargetPath = context.getLocalPathOfBranch();
		if(StringUtils.isBlank(appExtend.getPackageTargetPath())) {
			fullTargetPath += "target/";
			if(PackageBuildTypeEnum.GRADLE.getCode().equals(appExtend.getPackageBuildType())) {
				fullTargetPath += "build/";
			}
		}else {
			fullTargetPath += appExtend.getPackageTargetPath();
		}
		File packageTargetPath = Paths.get(fullTargetPath).toFile();
		if (!packageTargetPath.exists()) {
			logger.error("The target path does not exist");
			LogUtils.throwException(logger, MessageCodeEnum.PACK_FAILURE);
			return;
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
			LogUtils.throwException(logger, MessageCodeEnum.PACK_FAILURE);
		} else if (targetFiles.size() > 1) {
			logger.error("Multiple target files exist");
			LogUtils.throwException(logger, MessageCodeEnum.PACK_FAILURE);
		}

		//基础镜像
		String baseImage = baseImage(context);
		String fileNameWithExtension = targetFiles.get(0).toFile().getName();
		List<String> entrypoint = Arrays.asList("java", "-jar", fileNameWithExtension);
		doBuildImage(context, baseImage, entrypoint, targetFiles);
	}

	private void buildNodeImage(DeploymentContext context) {
		if(!TechTypeEnum.VUE.getCode().equals(context.getApp().getTechType())
				&& !TechTypeEnum.REACT.getCode().equals(context.getApp().getTechType())) {
			return;
		}

		AppExtendNode appExtend = context.getApp().getAppExtend();
		String fullDistPath = context.getLocalPathOfBranch();
		if(StringUtils.isBlank(appExtend.getPackageTargetPath())) {
			if(TechTypeEnum.VUE.getCode().equals(context.getApp().getTechType())) {
				fullDistPath += "dist/";
			}else if(TechTypeEnum.REACT.getCode().equals(context.getApp().getTechType())) {
				fullDistPath += "build/";
			}
		}else {
			fullDistPath += appExtend.getPackageTargetPath();
		}
		File distFile = new File(fullDistPath);
		if (!distFile.exists()) {
			logger.error("The target path does not exist");
			return;
		}

		//创建app的编译文件
		File appFile = new File(context.getLocalPathOfBranch() + context.getApp().getAppName());
		try {
			FileUtils.moveDirectory(distFile, appFile);
		} catch (IOException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.COPY_FILE_FAILURE);
		}

		doBuildImage(context, Constants.BUSYBOX_IMAGE_URL, null, Arrays.asList(appFile.toPath()));
	}

	private void buildNodejsImage(DeploymentContext context) {
		if(!TechTypeEnum.NODEJS.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		File branchFile = new File(context.getLocalPathOfBranch());
		File targetFile = new File(branchFile.getParent() + "/" + context.getApp().getAppName());
		rename(branchFile, targetFile);
		doBuildImage(context, context.getApp().getBaseImage(), null, Arrays.asList(targetFile.toPath()));
	}
	
	private void buildNuxtImage(DeploymentContext context) {
		if(!TechTypeEnum.NUXT.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		
		AppExtendNuxt appExted = context.getApp().getAppExtend();
		File branchFile = new File(context.getLocalPathOfBranch());
		File targetFile = new File(branchFile.getParent() + "/" + context.getApp().getAppName());
		rename(branchFile, targetFile);
		String baseImage = Constants.NODE_IMAGE_BASE_URL + appExted.getNodeVersion().substring(1);
		doBuildImage(context, baseImage, null, Arrays.asList(targetFile.toPath()));
	}

	private void buildHtmlImage(DeploymentContext context) {
		if(!TechTypeEnum.HTML.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		File branchFile = new File(context.getLocalPathOfBranch());
		File targetFile = new File(branchFile.getParent() + "/" + context.getApp().getAppName());
		rename(branchFile, targetFile);
		doBuildImage(context, context.getApp().getBaseImage(), null, Arrays.asList(targetFile.toPath()));
	}

	private void buildGoImage(DeploymentContext context) {
		if(!TechTypeEnum.GO.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		App app = context.getApp();
		File targetFile = new File(context.getLocalPathOfBranch() + app.getAppName());
		String baseImage = Constants.BUSYBOX_IMAGE_URL;
		String executableFile = Constants.USR_LOCAL_HOME + app.getAppName();
		List<String> entrypoint = Arrays.asList("chmod", "+x", executableFile, executableFile);
		doBuildImage(context, baseImage, entrypoint, Arrays.asList(targetFile.toPath()));
	}

	private void buildPythonImage(DeploymentContext context) {
		if(!TechTypeEnum.PYTHON.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		List<String> entrypoint = Arrays.asList("python", "main.py");
		buildPythonImage(context, entrypoint);
	}

	private void buildFlaskImage(DeploymentContext context) {
		if(!TechTypeEnum.FLASK.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		List<String> entrypoint = Arrays.asList("flask", "run");
		buildPythonImage(context, entrypoint);
	}

	private void buildDjangoImage(DeploymentContext context) {
		if(!TechTypeEnum.DJANGO.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		List<String> entrypoint = Arrays.asList("python", "manage.py", "runserver");
		buildPythonImage(context, entrypoint);
	}

	private void buildPythonImage(DeploymentContext context, List<String> entrypoint) {
		App app = context.getApp();
		File branchFile = new File(context.getLocalPathOfBranch());
		File targetFile = new File(branchFile.getParent() + "/" + app.getAppName());
		rename(branchFile, targetFile);
		String version = ((AppExtendPython)app.getAppExtend()).getPythonVersion();
		String baseImage = Constants.PYTHON_IMAGE_BASE_URL + version.substring(1);
		doBuildImage(context, baseImage, entrypoint, Arrays.asList(targetFile.toPath()));
	}

	private boolean rename(File source, File target) {
		boolean success = false;
		//如果30秒还没成功，则报错
		for(int i = 0; i < 300; i++) {
			if(source.renameTo(target)) {
				success = true;
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//ignore
			}
		}
		if(!success) {
			LogUtils.throwException(logger, MessageCodeEnum.RENAME_FILE_FAILURE);
		}
		return success;
	}

	private void doBuildImage(DeploymentContext context, String baseImageName, List<String> entrypoint, List<Path> targetFiles) {
		ImageRepo imageRepo = context.getGlobalConfigAgg().getImageRepo();
		String imageUrl = imageRepo.getUrl();
		String imageServer = imageUrl.substring(imageUrl.indexOf("//") + 2);

		logger.info("The base image is {}", baseImageName);

		targetFiles.forEach(e -> {
			logger.info("The target file is {}", e.toFile().getPath());
		});


		//Jib环境变量
		jibProperty();

		try {
			RegistryImage baseImage = RegistryImage.named(baseImageName);
			if(baseImageName.startsWith(imageServer)) {
				baseImage.addCredential(imageRepo.getAuthName(), imageRepo.getAuthPassword());
			}
			RegistryImage toImage = RegistryImage.named(context.getFullNameOfImage()).addCredential(
					imageRepo.getAuthName(), imageRepo.getAuthPassword());
			Jib.from(baseImage)
				.addLayer(targetFiles, AbsoluteUnixPath.get(Constants.USR_LOCAL_HOME))
				.setEntrypoint(entrypoint)
				//对于由alpine构建的镜像，使用addVolume(AbsoluteUnixPath.fromPath(Paths.get("/etc/localtime")))代码时时区才会生效
				//但是，由于Jib不支持RUN命令，因此像RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime也无法使用，
				//不过，可以通过手动构建基础镜像来使用RUN，然后目标镜像再依赖基础镜像。
				.addEnvironmentVariable("TZ", "Asia/Shanghai")
				.containerize(Containerizer.to(toImage)
						.setAllowInsecureRegistries(true)
						.addEventHandler(LogEvent.class, logEvent -> logger.info(logEvent.getMessage())));
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.BUILD_IMAGE);
		}
	}

	private String baseImage(DeploymentContext context) {
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
			baseImage = Constants.BUSYBOX_IMAGE_URL;
		}
		return baseImage;
	}

	private boolean updateDeployStatus(DeploymentContext context, DeploymentStatusEnum status, Date startTime, Date endTime) {
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
		try {
			HttpUtils.post(url, JsonUtils.toJsonString(threadParam), cookieParam);
		}catch(Exception e) {
			//如果终止部署线程失败，只打印异常日志，不展示页面提示，因为不影响主流程
			logger.error("Failed to abort deployment thread", e);
		}
		if(DeploymentStatusEnum.DEPLOYING.getCode().equals(deploymentDetail.getDeploymentStatus())) {
			bizParam.setDeploymentStatus(DeploymentStatusEnum.DEPLOYED_FAILURE.getCode());
		}else if(DeploymentStatusEnum.ROLLBACKING.getCode().equals(deploymentDetail.getDeploymentStatus())) {
			bizParam.setDeploymentStatus(DeploymentStatusEnum.ROLLBACK_FAILURE.getCode());
		}
		deploymentDetailRepository.updateById(bizParam);
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
		return null;
	}
}