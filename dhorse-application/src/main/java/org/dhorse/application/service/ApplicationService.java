package org.dhorse.application.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.dhorse.api.enums.ClusterTypeEnum;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.vo.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.vo.GlobalConfigAgg.Maven;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.param.GlobalConfigQueryParam;
import org.dhorse.infrastructure.repository.AppEnvRepository;
import org.dhorse.infrastructure.repository.AppMemberRepository;
import org.dhorse.infrastructure.repository.AppRepository;
import org.dhorse.infrastructure.repository.ClusterRepository;
import org.dhorse.infrastructure.repository.DeploymentDetailRepository;
import org.dhorse.infrastructure.repository.DeploymentVersionRepository;
import org.dhorse.infrastructure.repository.GlobalConfigRepository;
import org.dhorse.infrastructure.repository.SysUserRepository;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.BasePO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.cluster.K8sClusterStrategy;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DeployContext;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;

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
	protected DeploymentDetailRepository deploymentDetailRepository;
	
	@Autowired
	protected DeploymentVersionRepository deploymentVersionRepository;
	
	@Autowired
	protected ComponentConstants componentConstants;
	
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
				GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode()));
		return globalConfigRepository.queryAgg(param);
	}
	
	public GlobalConfigAgg globalConfig(GlobalConfigQueryParam queryParam) {
		GlobalConfigParam configParam = new GlobalConfigParam();
		configParam.setId(queryParam.getGlobalConfigId());
		configParam.setItemType(queryParam.getItemType());
		return globalConfigRepository.queryAgg(configParam);
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
		return componentConstants.getDataPath() + "repository/";
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
			return new K8sClusterStrategy();
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
	
	protected String fullNameOfAgentImage(DeployContext context) {
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
			fullNameOfImage.append(Constants.IMAGE_REPOSITORY);
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
	
	protected CloseableHttpClient createHttpClient(String url) {
		if(!url.startsWith("https")) {
			return HttpClients.createDefault();
		}
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.SSL_CLIENT_FAILURE);
		}
		
		return HttpClients.createDefault();
	}
	
	public List<String> queryJavaVersion(){
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setItemType(GlobalConfigItemTypeEnum.MAVEN.getCode());
		GlobalConfigAgg globalConfigAgg = globalConfigRepository.queryAgg(bizParam);
		return queryJavaVersion(globalConfigAgg.getMaven());
	}
	
	public String queryJavaMajorVersion(Maven mavenConf){
		List<String> javaVersions = queryJavaVersion(mavenConf);
		String[] versions = javaVersions.get(0).split("\\.");
		if(Integer.valueOf(versions[0]) < 9) {
			return versions[0] + "." + versions[1];
		}
		return versions[0];
	}
	
	public List<String> queryJavaVersion(Maven mavenConf){
		String javaHome = null;
		if(mavenConf != null && !StringUtils.isBlank(mavenConf.getJavaHome())) {
			javaHome = mavenConf.getJavaHome();
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
