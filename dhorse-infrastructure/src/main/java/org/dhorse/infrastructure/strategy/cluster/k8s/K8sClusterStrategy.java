package org.dhorse.infrastructure.strategy.cluster.k8s;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.PackageFileTypeEnum;
import org.dhorse.api.enums.ReplicaStatusEnum;
import org.dhorse.api.enums.TechTypeEnum;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.cluster.ClusterNodePageParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespacePageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppEnv;
import org.dhorse.api.response.model.AppExtendJava;
import org.dhorse.api.response.model.ClusterNamespace;
import org.dhorse.api.response.model.ClusterNode;
import org.dhorse.api.response.model.EnvReplica;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.infrastructure.model.ReplicaMetrics;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.ClusterStrategy;
import org.dhorse.infrastructure.strategy.cluster.model.DockerConfigJson;
import org.dhorse.infrastructure.strategy.cluster.model.DockerConfigJson.Auth;
import org.dhorse.infrastructure.strategy.cluster.model.Replica;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DateUtils;
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.dhorse.infrastructure.utils.DeploymentThreadPoolUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.autoscaling.v1.CrossVersionObjectReference;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerSpec;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBackend;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.api.model.networking.v1.IngressServiceBackend;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpec;
import io.fabric8.kubernetes.api.model.networking.v1.ServiceBackendPort;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.utils.Serialization;

/**
 * k8s操作功能，采用fabric8客户端实现。
 * @author Dahai
 */
public class K8sClusterStrategy implements ClusterStrategy {

	private static final Logger logger = LoggerFactory.getLogger(K8sClusterStrategy.class);
	
	private static final String FILEBEAT_NAME = "filebeat";
	
	private static final String FILEBEAT_KUBEADM_NAME = "filebeat-kubeadm-config";
	
	private static final String FILEBEAT_CONFIG = "filebeat-config";
	
	private static final String NS = K8sUtils.KUBE_SYSTEM_NAMESPACE;
	
	@Override
	public Void createSecret(ClusterPO clusterPO, ImageRepo imageRepo) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			Secret secret = new Secret();
			secret.setType("kubernetes.io/dockerconfigjson");
			ObjectMeta meta = new ObjectMeta();
			meta.setName(K8sUtils.DOCKER_REGISTRY_KEY);
			meta.setLabels(K8sUtils.dhorseLabel(K8sUtils.DOCKER_REGISTRY_KEY));
			secret.setMetadata(meta);
			secret.setData(dockerConfigData(imageRepo));
			NamespaceList namespaceList = client.namespaces().list();
			if(CollectionUtils.isEmpty(namespaceList.getItems())) {
				return null;
			}
			for(Namespace n : namespaceList.getItems()) {
				String namespace = n.getMetadata().getName();
				if(!K8sUtils.DHORSE_NAMESPACE.equals(namespace)
						&& K8sUtils.getSystemNamspaces().contains(namespace)) {
					continue;
				}
				if(!"Active".equals(n.getStatus().getPhase())){
					continue;
				}
				Resource<Secret> resource = client.secrets().inNamespace(namespace).resource(secret);
				this.doOperation(resource);
			}
		}
		return null;
	}
	
	private Map<String, String> dockerConfigData(ImageRepo imageRepo) {
		Map<String, String> data = new HashMap<>();
		Encoder encoder = Base64.getEncoder();
		String auth = encoder.encodeToString((imageRepo.getAuthName() + ":" + imageRepo.getAuthPassword()).getBytes());
		Auth auths = new Auth();
		auths.setUsername(imageRepo.getAuthName());
		auths.setPassword(imageRepo.getAuthPassword());
		auths.setAuth(auth);
		DockerConfigJson dockerConfigJson = new DockerConfigJson();
		dockerConfigJson.setAuths(Collections.singletonMap(imageRepo.getUrl(), auths));
		data.put(".dockerconfigjson", encoder.encodeToString(JsonUtils.toJsonString(dockerConfigJson).getBytes()));
		return data;
	}
	
	@Override
	public Replica readDeployment(ClusterPO clusterPO, AppEnv appEnv, AppPO appPO) {
		String deploymentName = K8sUtils.getDeploymentName(appPO.getAppName(), appEnv.getTag());
		Deployment deployment = null;
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			deployment = client.apps().deployments().inNamespace(appEnv.getNamespaceName())
					.withName(deploymentName).get();
		}
		if(deployment == null) {
			return null;
		}
		PodSpec podSpec = deployment.getSpec().getTemplate().getSpec();
		Replica replica = new Replica();
		replica.setImageName(imageName(appPO, podSpec));
		return replica;
	}
	
	public boolean createDeployment(DeploymentContext context) {
		logger.info("Start to deploy k8s server");
		String namespace = context.getAppEnv().getNamespaceName();
		String labelSelector = K8sUtils.getSelectorKey(context.getDeploymentName());
		try(KubernetesClient client = client(context.getCluster().getClusterUrl(),
				context.getCluster().getAuthToken())){

			//执行deployment
			Resource<Deployment> resource = client.apps().deployments()
					.inNamespace(namespace).resource(K8sDeploymentHelper.build(context));
			doOperation(resource);
			
			// 自动扩容任务
			createAutoScaling(context.getAppEnv(), context.getDeploymentName(), client);
	
			if(!checkHealthOfAll(client, namespace, labelSelector)) {
				logger.error("Failed to create k8s deployment, because the replica is not fully started");
				LogUtils.throwException(logger, MessageCodeEnum.CREATE_PART_POD);
				return false;
			}
				
			//创建ClusterIP类型的Service
			createService(context, client);
			
			//创建Ingress
			createIngress(context, client);
		}
			
		logger.info("End to deploy k8s server");
		return true;
	}
	
	private void doOperation(Resource<?> resource) {
		if (resource.get() == null) {
			resource.create();
		} else {
			resource.update();
		}
	}
	
	private boolean createService(DeploymentContext context, KubernetesClient client) {
		String namespace = context.getAppEnv().getNamespaceName();
		String serviceName = K8sUtils.getServiceName(context.getApp().getAppName(), context.getAppEnv().getTag());
		Service service = new Service();
		service.setMetadata(serviceMeta(serviceName, context));
		service.setSpec(serviceSpec(context));
		Resource<Service> resource = client.services().inNamespace(namespace).resource(service);
		this.doOperation(resource);
		return true;
	}
	
	private boolean createIngress(DeploymentContext context, KubernetesClient client) {
		String ingressHost = context.getAppEnv().getIngressHost();
		String namespace = context.getAppEnv().getNamespaceName();
		String ingressName = K8sUtils.getServiceName(context.getApp().getAppName(), context.getAppEnv().getTag());
		Ingress ingress = new Ingress();
		ingress.setMetadata(ingressMeta(ingressName, context));
		ingress.setSpec(ingressSpec(context, ingressName, ingressHost));
		Resource<Ingress> resource = client.network().v1()
				.ingresses().inNamespace(namespace).resource(ingress);
		if(StringUtils.isBlank(ingressHost)) {
			resource.delete();
		}else {
			doOperation(resource);
		}
		return true;
	}
	
	private ObjectMeta ingressMeta(String appName, DeploymentContext context) {
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName(appName);
		metadata.setLabels(K8sUtils.dhorseLabel(appName));
		metadata.setAnnotations(PrometheusHelper.addPrometheus("Ingress", context));
		return metadata;
	}
	
	private IngressSpec ingressSpec(DeploymentContext context, String ingressName, String ingressHost) {
		ServiceBackendPort serviceBackendPort = new ServiceBackendPort();
		serviceBackendPort.setNumber(context.getAppEnv().getServicePort());
		IngressServiceBackend ingressServiceBackend = new IngressServiceBackend();
		ingressServiceBackend.setName(ingressName);
		ingressServiceBackend.setPort(serviceBackendPort);
		IngressBackend ingressBackend = new IngressBackend();
		ingressBackend.setService(ingressServiceBackend);
		
		HTTPIngressPath path = new HTTPIngressPath();
		path.setPath("/");
		path.setPathType("Prefix");
		path.setBackend(ingressBackend);
		HTTPIngressRuleValue http = new HTTPIngressRuleValue();
		http.setPaths(Arrays.asList(path));
		IngressRule ingressRule = new IngressRule();
		ingressRule.setHost(ingressHost);
		ingressRule.setHttp(http);
		
		IngressSpec spec = new IngressSpec();
		spec.setIngressClassName(Constants.NGINX);
		spec.setDefaultBackend(ingressBackend);
		spec.setRules(Arrays.asList(ingressRule));
		return spec;
	}
	
	public boolean deleteDeployment(ClusterPO clusterPO, AppPO appPO, AppEnvPO appEnvPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			String namespace = appEnvPO.getNamespaceName();
			String depolymentName = K8sUtils.getDeploymentName(appPO.getAppName(), appEnvPO.getTag());
			List<StatusDetails> statusList = client.apps().deployments().inNamespace(namespace)
					.withName(depolymentName).delete();
			if(!isSuccess(statusList)) {
				return false;
			}
			if(!deleteAutoScaling(namespace, depolymentName, client)) {
				return false;
			}
			String serviceName = K8sUtils.getServiceName(appPO.getAppName(), appEnvPO.getTag());
			if(!deleteService(namespace, serviceName, client)) {
				return false;
			}
			if(!deleteIngress(namespace, serviceName, client)) {
				return false;
			}
		}
		return true;
	}

	public boolean autoScaling(AppPO appPO, AppEnvPO appEnvPO, ClusterPO clusterPO) {
		String deploymentName = K8sUtils.getReplicaAppName(appPO.getAppName(), appEnvPO.getTag());
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			return createAutoScaling(appEnvPO, deploymentName, client);
		}
	}

	private boolean createAutoScaling(AppEnvPO appEnvPO, String deploymentName, KubernetesClient client) {
		HorizontalPodAutoscaler body = new HorizontalPodAutoscaler();
		body.setKind("HorizontalPodAutoscaler");
		body.setApiVersion("autoscaling/v1");
		HorizontalPodAutoscalerSpec spec = new HorizontalPodAutoscalerSpec();
		spec.setMinReplicas(appEnvPO.getMinReplicas());
		spec.setMaxReplicas(appEnvPO.getMaxReplicas());
		CrossVersionObjectReference scaleTargetRef = new CrossVersionObjectReference();
		scaleTargetRef.setApiVersion("apps/v1");
		scaleTargetRef.setKind("Deployment");
		scaleTargetRef.setName(deploymentName);
		spec.setScaleTargetRef(scaleTargetRef);
		spec.setTargetCPUUtilizationPercentage(appEnvPO.getAutoScalingCpu());
		body.setMetadata(deploymentMetaData(deploymentName));
		body.setSpec(spec);
		Resource<HorizontalPodAutoscaler> resource = client.autoscaling().v1()
				.horizontalPodAutoscalers().inNamespace(appEnvPO.getNamespaceName()).resource(body);
		this.doOperation(resource);
		return true;
	}
	
	private boolean deleteAutoScaling(String namespace, String deploymentName, KubernetesClient client) {
		List<StatusDetails> statusList = client.autoscaling().v1().horizontalPodAutoscalers()
				.inNamespace(namespace).withName(deploymentName).delete();
		return isSuccess(statusList);
	}
	
	private boolean deleteService(String namespace, String serviceName, KubernetesClient client) {
		List<StatusDetails> statusList = client.services().inNamespace(namespace)
				.withName(serviceName).delete();
		return isSuccess(statusList);
	}
	
	private boolean deleteIngress(String namespace, String serviceName, KubernetesClient client) {
		List<StatusDetails> statusList = client.network().v1().ingresses()
				.inNamespace(namespace).withName(serviceName).delete();
		return isSuccess(statusList);
	}
	
	private boolean isSuccess(List<StatusDetails> statusList) {
		return statusList.stream().map(e -> CollectionUtils.isEmpty(e.getCauses()))
				.reduce((e1, e2) -> e1 && e2).orElse(true);
	}

	private ObjectMeta serviceMeta(String appName, DeploymentContext context) {
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName(appName);
		metadata.setLabels(K8sUtils.dhorseLabel(appName));
		metadata.setAnnotations(PrometheusHelper.addPrometheus("Service", context));
		return metadata;
	}
	
	private ServiceSpec serviceSpec(DeploymentContext context) {
		ServiceSpec spec = new ServiceSpec();
		spec.setSelector(Collections.singletonMap(K8sUtils.DHORSE_LABEL_KEY, context.getDeploymentName()));
		spec.setPorts(servicePorts(context));
		spec.setType("ClusterIP");
		return spec;
	}
	
	private List<ServicePort> servicePorts(DeploymentContext context){
		//主端口
		ServicePort servicePort = new ServicePort();
		servicePort.setName("major");
		servicePort.setProtocol("TCP");
		servicePort.setPort(context.getAppEnv().getServicePort());
		servicePort.setTargetPort(new IntOrString(context.getAppEnv().getServicePort()));
		List<ServicePort> ports = new ArrayList<>();
		ports.add(servicePort);
		
		//辅助端口
		if(!StringUtils.isBlank(context.getAppEnv().getMinorPorts())) {
			String[] portStr = context.getAppEnv().getMinorPorts().split(",");
			for(int i = 0; i < portStr.length; i++) {
				ServicePort one = new ServicePort();
				Integer port = Integer.valueOf(portStr[i]);
				one.setName("minor" + (i + 1));
				one.setProtocol("TCP");
				one.setPort(port);
				one.setTargetPort(new IntOrString(port));
				ports.add(one);
			}
		}
		return ports;
	}
	
	private ObjectMeta deploymentMetaData(String deploymentName) {
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName(deploymentName);
		metadata.setLabels(K8sUtils.dhorseLabel(deploymentName));
		return metadata;
	}
	
	private boolean warFileType(AppPO appPO) {
		if(!springBootApp(appPO)) {
			return false;
		}
		AppExtendJava appExtend = JsonUtils.parseToObject(appPO.getExt(), AppExtendJava.class);
		return PackageFileTypeEnum.WAR.getCode().equals(appExtend.getPackageFileType());
	}
	
	private boolean springBootApp(AppPO appPO) {
		return TechTypeEnum.SPRING_BOOT.getCode().equals(appPO.getTechType());
	}

	/**
	 * Nginx服务，如：Vue、React、Html
	 */
	private boolean nginxApp(AppPO appPO) {
		return TechTypeEnum.VUE.getCode().equals(appPO.getTechType())
				|| TechTypeEnum.REACT.getCode().equals(appPO.getTechType())
				|| TechTypeEnum.HTML.getCode().equals(appPO.getTechType());
	}

	private boolean checkHealthOfAll(KubernetesClient client, String namespace, String labelSelector) {
		// 检查pod状态，检查时长20分钟
		for (int i = 0; i < 60 * 20; i++) {
			if(DeploymentThreadPoolUtils.isInterrupted()) {
				return false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
			PodList podList = client.pods()
					.inNamespace(namespace)
					.withLabelSelector(labelSelector)
					.list();
			if (CollectionUtils.isEmpty(podList.getItems())) {
				logger.warn("Replica size is 0");
				continue;
			}
			if (doCheckHealth(podList.getItems())) {
				logger.info("All replicas successfullly");
				return true;
			}
		}
		
		return false;
	}
	
	private boolean doCheckHealth(List<Pod> pods) {
		int runningService = 0;
		for (Pod pod : pods) {
			if (ReplicaStatusEnum.RUNNING.getCode().compareTo(podStatus(pod.getStatus())) == 0) {
				runningService++;
			}else {
				logger.info("{} is starting", pod.getMetadata().getName());
			}
		}
		return runningService == pods.size();
	}

	public PageData<EnvReplica> replicaPage(EnvReplicaPageParam pageParam, ClusterPO clusterPO,
			AppPO appPO, AppEnvPO appEnvPO) {
		String namespace = appEnvPO.getNamespaceName();
		PodList podList = null;
		String labelSelector = K8sUtils.getDeploymentLabelSelector(appPO.getAppName(), appEnvPO.getTag());
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			podList = client.pods()
					.inNamespace(namespace)
					.withLabelSelector(labelSelector)
					.list();
		}
		PageData<EnvReplica> pageData = new PageData<>();
		int dataCount = podList.getItems().size();
		if (dataCount == 0) {
			pageData.setPageNum(1);
			pageData.setPageCount(0);
			pageData.setPageSize(pageParam.getPageSize());
			pageData.setItemCount(0);
			return pageData;
		}

		int pageCount = dataCount / pageParam.getPageSize();
		if (dataCount % pageParam.getPageSize() > 0) {
			pageCount += 1;
		}
		int pageNum = pageParam.getPageNum() > pageCount ? pageCount : pageParam.getPageNum();
		int startOffset = (pageNum - 1) * pageParam.getPageSize();
		int endOffset = pageNum * pageParam.getPageSize();
		endOffset = endOffset > dataCount ? dataCount : endOffset;
		List<Pod> pagePod = podList.getItems().subList(startOffset, endOffset);
		List<EnvReplica> pods = pagePod.stream().map(e -> {
			String imageName = imageName(appPO, e.getSpec());
			EnvReplica r = new EnvReplica();
			if(!StringUtils.isBlank(imageName)) {
				r.setVersionName(imageName.substring(imageName.lastIndexOf("/") + 1));
			}
			r.setIp(e.getStatus().getPodIP());
			r.setName(e.getMetadata().getName());
			r.setEnvName(appEnvPO.getEnvName());
			r.setClusterName(clusterPO.getClusterName());
			r.setNamespace(namespace);
			//这里为了解决k8s的时区问题，强制用东8区转换
			r.setStartTime(DateUtils.formatLocal(e.getMetadata().getCreationTimestamp()));
			r.setStatus(podStatus(e.getStatus()));
			r.setNodeName(e.getSpec().getNodeName());
			return r;
		}).collect(Collectors.toList());
		pageData.setItems(pods);
		pageData.setPageNum(pageNum);
		pageData.setPageCount(pageCount);
		pageData.setPageSize(pageParam.getPageSize());
		pageData.setItemCount(dataCount);

		return pageData;
	}
	
	private String imageName(AppPO appPO, PodSpec podSpec) {
		if(warFileType(appPO)) {
			for(Container initC : podSpec.getInitContainers()){
				if("war".equals(initC.getName())) {
					return initC.getImage();
				}
			}
		}else if(nginxApp(appPO)) {
			for(Container initC : podSpec.getInitContainers()){
				if(Constants.NGINX.equals(initC.getName())) {
					return initC.getImage();
				}
			}
		}else {
			return podSpec.getContainers().get(0).getImage();
		} 
		return null;
	}
	
	private Integer podStatus(PodStatus podStatus) {
		if("Pending".equals(podStatus.getPhase())) {
			return ReplicaStatusEnum.PENDING.getCode();
		}
		for(ContainerStatus containerStatus : podStatus.getContainerStatuses()) {
			ContainerStateTerminated terminated = containerStatus.getState().getTerminated();
			if(terminated != null) {
				if(terminated.getExitCode() == null) {
					return ReplicaStatusEnum.FAILED.getCode();
				}
				if(terminated.getExitCode() == 1 || terminated.getExitCode() == 137
						|| terminated.getExitCode() == 139 || terminated.getExitCode() == 143) {
					return ReplicaStatusEnum.DESTROYING.getCode();
				}
				return ReplicaStatusEnum.FAILED.getCode();
			}
			if(!containerStatus.getStarted().booleanValue() || !containerStatus.getReady().booleanValue()) {
				return ReplicaStatusEnum.PENDING.getCode();
			}
		}
		for(PodCondition c : podStatus.getConditions()) {
			if(!"True".equals(c.getStatus())) {
				return ReplicaStatusEnum.FAILED.getCode();
			}
		}
		return ReplicaStatusEnum.RUNNING.getCode();
	}

	public List<ReplicaMetrics> replicaMetrics(ClusterPO clusterPO, String namespace) {
		PodMetricsList podMetricsList = null;
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			if (!client.supports(NodeMetrics.class)) {
				logger.warn("Metrics API is not enabled in your cluster");
				return null;
			}
			podMetricsList = client.top().pods().metrics();
		}
		if(podMetricsList == null) {
			return null;
		}
		List<PodMetrics> metrics = podMetricsList.getItems();
		List<ReplicaMetrics> replicaMetrics = new ArrayList<>(metrics.size());
		for(PodMetrics metric : metrics) {
			if(CollectionUtils.isEmpty(metric.getContainers())) {
				continue;
			}
			String replicaName = metric.getMetadata().getName();
			Map<String, Quantity> usage = metric.getContainers().get(0).getUsage();
			//转换为单位：m
			long cpuUsed = new BigDecimal(usage.get("cpu").getAmount()).movePointLeft(6).setScale(0, RoundingMode.HALF_UP).longValue();
			//转换为单位：字节
			long memoryUsed = new BigDecimal(usage.get("memory").getAmount()).movePointRight(3).setScale(0, RoundingMode.HALF_UP).longValue();
			replicaMetrics.add(ReplicaMetrics.of(replicaName, cpuUsed, memoryUsed));
		}
		return replicaMetrics;
	}
	
	@Override
	public boolean rebuildReplica(ClusterPO clusterPO, String replicaName, String namespace) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			client.pods().inNamespace(namespace).withName(replicaName).delete();
			return true;
		}
	}

	public InputStream streamPodLog(ClusterPO clusterPO, String replicaName, String namespace) {
		KubernetesClient client = client(clusterPO.getClusterUrl(),
				clusterPO.getAuthToken(), 1 * 1000, 5 * 60 * 1000);
			LogWatch watch = client.pods().inNamespace(namespace).withName(replicaName)
		    		.tailingLines(2000).watchLog();
			return watch.getOutput();
	}
	
	public String podLog(ClusterPO clusterPO, String replicaName, String namespace) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			//目前只支持下载最近50万行的日志
			return client.pods().inNamespace(namespace).withName(replicaName)
		    		.tailingLines(500000).getLog();
		}
	}

	public String podYaml(ClusterPO clusterPO, String replicaName, String namespace) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			return Serialization.asYaml(client.pods()
					.inNamespace(namespace).withName(replicaName).get());
		}
	}

	public void openLogCollector(ClusterPO clusterPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			File beatFile = new File(Constants.CONF_PATH + "kubernetes-filebeat.yml");
			if (!beatFile.exists()) {
				LogUtils.throwException(logger, MessageCodeEnum.FILE_BEAT_K8S_FILE_INEXISTENCE);
			}
			
			List<HasMetadata> metas = null;
			try (InputStream in = new FileInputStream(beatFile)){
				metas = client.load(in).items();
			} catch (Exception e) {
				LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
			}
			
			for (HasMetadata o : metas) {
				//ServiceAccount
				if (o instanceof ServiceAccount) {
					Resource<ServiceAccount> resource = client.serviceAccounts().resource((ServiceAccount)o);
					doOperation(resource);
					continue;
				}
				
				//ClusterRole
				if (o instanceof ClusterRole) {
					Resource<ClusterRole> resource = client.rbac().clusterRoles().resource((ClusterRole)o);
					doOperation(resource);
					continue;
				}
				
				//Role
				if (o instanceof Role) {
					Resource<Role> resource = client.rbac().roles().resource((Role) o);
					doOperation(resource);
					continue;
				}
				
				//ClusterRoleBinding
				if (o instanceof ClusterRoleBinding) {
					Resource<ClusterRoleBinding> resource = client.rbac().clusterRoleBindings().resource((ClusterRoleBinding) o);
					doOperation(resource);
					continue;
				}
				
				//RoleBinding
				if (o instanceof RoleBinding) {
					Resource<RoleBinding> resource = client.rbac().roleBindings().resource((RoleBinding) o);
					doOperation(resource);
					continue;
				}
				
				//ConfigMap
				if (o instanceof ConfigMap) {
					Resource<ConfigMap> resource = client.configMaps().resource((ConfigMap) o);
					doOperation(resource);
					continue;
				}
				
				//DaemonSet
				if (o instanceof DaemonSet) {
					Resource<DaemonSet> resource = client.apps().daemonSets().resource((DaemonSet) o);
					doOperation(resource);
					continue;
				}
			}
		}
	}

	public void closeLogCollector(ClusterPO clusterPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			client.serviceAccounts().inNamespace(NS).withName(FILEBEAT_NAME).delete();
			client.rbac().clusterRoles().withName(FILEBEAT_NAME).delete();
			client.rbac().roles().inNamespace(NS).withName(FILEBEAT_NAME).delete();
			client.rbac().roles().inNamespace(NS).withName(FILEBEAT_KUBEADM_NAME).delete();
			client.rbac().clusterRoleBindings().withName(FILEBEAT_NAME).delete();
			client.rbac().roleBindings().inNamespace(NS).withName(FILEBEAT_NAME).delete();
			client.rbac().roleBindings().inNamespace(NS).withName(FILEBEAT_KUBEADM_NAME).delete();
			client.configMaps().inNamespace(NS).withName(FILEBEAT_CONFIG).delete();
			client.apps().daemonSets().inNamespace(NS).withName(FILEBEAT_NAME).delete();
		}
	}
	
	public boolean logSwitchStatus(ClusterPO clusterPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			return client.apps().daemonSets()
					.inNamespace(NS)
					.withName(FILEBEAT_NAME)
					.get() != null;
		}
	}
	
	public List<String> queryFiles(ClusterPO clusterPO, String replicaName, String namespace) {
		String[] commands = new String[] {"sh", "-c", "ls", K8sUtils.TMP_DATA_PATH};
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
				ExecWatch watch = client.pods().inNamespace(namespace).withName(replicaName)
				        .writingOutput(System.out)
						.exec(commands)){
		}
		return null;
	}
	
	public InputStream downloadFile(ClusterPO clusterPO, String namespace,  String replicaName, String fileName) {
		return null;
	}
	
	public List<ClusterNamespace> namespaceList(ClusterPO clusterPO,
			ClusterNamespacePageParam pageParam) {
		NamespaceList namespaceList = null;
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			ListOptions o = new ListOptions();
			if(pageParam != null && !StringUtils.isBlank(pageParam.getNamespaceName())) {
				o.setLabelSelector("kubernetes.io/metadata.name=" + pageParam.getNamespaceName());
			}
			namespaceList = client.namespaces().list(o);
		}
		List<ClusterNamespace> namespaces = new ArrayList<>();
		if(CollectionUtils.isEmpty(namespaceList.getItems())) {
			return namespaces;
		}
		for(Namespace n : namespaceList.getItems()) {
			if(K8sUtils.getSystemNamspaces().contains(n.getMetadata().getName())) {
				continue;
			}
			if(!"Active".equals(n.getStatus().getPhase())){
				continue;
			}
			ClusterNamespace one = new ClusterNamespace();
			one.setNamespaceName(n.getMetadata().getName());
			namespaces.add(one);
		}
		return namespaces;
	}
	
	public boolean addNamespace(ClusterPO clusterPO, String namespaceName) {
		ObjectMeta metaData = new ObjectMeta();
		metaData.setName(namespaceName);
		metaData.setLabels(Collections.singletonMap("custom.name", namespaceName));
		Namespace namespace = new Namespace();
		namespace.setMetadata(metaData);
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			Resource<Namespace> resource = client.namespaces().resource(namespace);
			if(resource.get() != null) {
				logger.info("The namespace {} already exists ", namespaceName);
				return true;
			}
			resource.create();
		}
		return true;
	}
	
	public boolean deleteNamespace(ClusterPO clusterPO, String namespaceName) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			client.namespaces().withName(namespaceName).delete();
		}
		return true;
	}
	
	/**
	 * 通过ConfigMap向k8s集群写入dhorse服务器的地址，地址格式为：ip1:8100,ip2:8100
	 */
	@Override
	public Void createDHorseConfig(ClusterPO clusterPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			DHorseConfigHelper.writeServerIp(clusterPO, client);
		}
		return null;
	}
	
	/**
	 * 通过ConfigMap向k8s集群删除dhorse服务器的地址，地址格式为：ip1:8100,ip2:8100
	 */
	@Override
	public Void deleteDHorseConfig(ClusterPO clusterPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			DHorseConfigHelper.deleteServerIp(clusterPO, client);
		}
		return null;
	}
	
	public PageData<ClusterNode> nodePage(ClusterNodePageParam pageParam, ClusterPO clusterPO) {
		NodeList nodeList = null;
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			ListOptions o = new ListOptions();
			if(!StringUtils.isBlank(pageParam.getNodeName())) {
				o.setLabelSelector("kubernetes.io/hostname=" + pageParam.getNodeName());
			}
			nodeList = client.nodes().list(o);
		}
		
		PageData<ClusterNode> pageData = new PageData<>();
		int dataCount = nodeList.getItems().size();
		if (dataCount == 0) {
			pageData.setPageNum(1);
			pageData.setPageCount(0);
			pageData.setPageSize(pageParam.getPageSize());
			pageData.setItemCount(0);
			return pageData;
		}

		int pageCount = dataCount / pageParam.getPageSize();
		if (dataCount % pageParam.getPageSize() > 0) {
			pageCount += 1;
		}
		int pageNum = pageParam.getPageNum() > pageCount ? pageCount : pageParam.getPageNum();
		int startOffset = (pageNum - 1) * pageParam.getPageSize();
		int endOffset = pageNum * pageParam.getPageSize();
		endOffset = endOffset > dataCount ? dataCount : endOffset;
		List<Node> pageNode = nodeList.getItems().subList(startOffset, endOffset);
		List<ClusterNode> nodes = new ArrayList<>();
		for(Node n : pageNode) {
			ClusterNode node = new ClusterNode();
			node.setNodeName(n.getMetadata().getName());
			node.setCreationTime(DateUtils.formatLocal2(n.getMetadata().getCreationTimestamp()));
			for(NodeAddress address : n.getStatus().getAddresses()) {
				if("InternalIP".equals(address.getType())) {
					node.setNodeIp(address.getAddress());
					break;
				}
			}
			for(NodeCondition c : n.getStatus().getConditions()) {
				if("Ready".equals(c.getType())) {
					node.setStatus("True".equals(c.getStatus()) ? "正常" : "异常");
					break;
				}
			}
			nodes.add(node);
		}
		pageData.setItems(nodes);
		pageData.setPageNum(pageNum);
		pageData.setPageCount(pageCount);
		pageData.setPageSize(pageParam.getPageSize());
		pageData.setItemCount(dataCount);
		
		return pageData;
	}
	
	public String version(ClusterPO clusterPO) {
		try(KubernetesClient client = client(clusterPO.getClusterUrl(), clusterPO.getAuthToken())){
			VersionInfo versionInfo = client.getKubernetesVersion();
			return "v" + versionInfo.getMajor() + "." + versionInfo.getMinor();
		}
	}
	
	private KubernetesClient client(String basePath, String accessToken) {
		return client(basePath, accessToken, 1000, 1000);
	}
	
	private KubernetesClient client(String basePath, String accessToken, int connectTimeout, int readTimeout) {
		 Config config = new ConfigBuilder()
				 .withTrustCerts(true)
				 .withMasterUrl(basePath)
				 .withOauthToken(accessToken)
				 .withConnectionTimeout(connectTimeout)
				 .withRequestTimeout(readTimeout)
				 .withRequestRetryBackoffLimit(0)
				 .build();
		return new KubernetesClientBuilder()
				.withConfig(config)
				.build();
	}
}