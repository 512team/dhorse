package org.dhorse.application.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.ClusterTypeEnum;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.param.GlobalConfigQueryParam;
import org.dhorse.infrastructure.param.ProjectMemberParam;
import org.dhorse.infrastructure.repository.ClusterRepository;
import org.dhorse.infrastructure.repository.DeploymentDetailRepository;
import org.dhorse.infrastructure.repository.DeploymentVersionRepository;
import org.dhorse.infrastructure.repository.GlobalConfigRepository;
import org.dhorse.infrastructure.repository.ProjectEnvRepository;
import org.dhorse.infrastructure.repository.ProjectExtendJavaRepository;
import org.dhorse.infrastructure.repository.ProjectMemberRepository;
import org.dhorse.infrastructure.repository.ProjectRepository;
import org.dhorse.infrastructure.repository.SysUserRepository;
import org.dhorse.infrastructure.repository.po.BasePO;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
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
	protected ProjectRepository projectRepository;
	
	@Autowired
	protected ProjectExtendJavaRepository projectExtendJavaRepository;

	@Autowired
	protected ProjectMemberRepository projectMemberRepository;

	@Autowired
	protected ProjectEnvRepository projectEnvRepository;
	
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
		configParam.setItemType(queryParam.getItemType());
		return globalConfigRepository.queryAgg(configParam);
	}
	
	public ProjectPO validateProject(String projectId) {
		ProjectPO projectPO = projectRepository.queryById(projectId);
		if(projectPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.PROJECT_IS_INEXISTENCE);
		}
		return projectPO;
	}
	
	protected ClusterStrategy clusterStrategy(Integer clusterType) {
		if (ClusterTypeEnum.K8S.getCode().equals(clusterType)) {
			return new K8sClusterStrategy();
		} else {
			return new K8sClusterStrategy();
		}
	}
	
	protected void hasRights(LoginUser loginUser, String projectId) {
		if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())){
			return;
		}
		ProjectMemberParam projectMemberParam = new ProjectMemberParam();
		projectMemberParam.setProjectId(projectId);
		projectMemberParam.setUserId(loginUser.getId());
		ProjectMemberPO projectMemberPO = projectMemberRepository.query(projectMemberParam);
		if(Objects.isNull(projectMemberPO)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
	}
	
	protected void hasAdminRights(LoginUser loginUser, String projectId) {
		if(RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())){
			return;
		}
		ProjectMemberParam projectMemberParam = new ProjectMemberParam();
		projectMemberParam.setProjectId(projectId);
		projectMemberParam.setUserId(loginUser.getId());
		ProjectMemberPO projectMemberPO = projectMemberRepository.query(projectMemberParam);
		if(Objects.isNull(projectMemberPO)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		String[] roleTypes = projectMemberPO.getRoleType().split(",");
		Set<Integer> roleSet = new HashSet<>();
		for (String role : roleTypes) {
			roleSet.add(Integer.valueOf(role));
		}
		List<Integer> adminRole = Constants.ROLE_OF_OPERATE_PROJECT_USER.stream()
				.filter(item -> roleSet.contains(item))
				.collect(Collectors.toList());
		if(adminRole.size() <= 0) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
	}
	
	protected String fullNameOfAgentImage(DeployContext context) {
		if(!YesOrNoEnum.YES.getCode().equals(context.getProjectEnv().getTraceStatus())) {
			return null;
		}
		TraceTemplate traceTemplate = context.getGlobalConfigAgg().getTraceTemplate(context.getProjectEnv().getTraceTemplateId());
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
			fullNameOfImage.append(imageRepo.getAuthUser());
		}else {
			fullNameOfImage.append(Constants.IMAGE_REPO_PROJECT);
		}
		fullNameOfImage.append("/").append(nameOfImage);
		return fullNameOfImage.toString();
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
