package org.dhorse.infrastructure.strategy.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.ActionTypeEnum;
import org.dhorse.api.enums.AffinityLevelEnum;
import org.dhorse.api.enums.ImageSourceEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.NginxVersionEnum;
import org.dhorse.api.enums.PackageFileTypeEnum;
import org.dhorse.api.enums.ReplicaStatusEnum;
import org.dhorse.api.enums.SchedulingTypeEnum;
import org.dhorse.api.enums.TechTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespacePageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.App;
import org.dhorse.api.response.model.AppEnv;
import org.dhorse.api.response.model.AppEnv.EnvExtendNode;
import org.dhorse.api.response.model.AppEnv.EnvExtendSpringBoot;
import org.dhorse.api.response.model.AppExtendJava;
import org.dhorse.api.response.model.ClusterNamespace;
import org.dhorse.api.response.model.EnvHealth;
import org.dhorse.api.response.model.EnvHealth.Item;
import org.dhorse.api.response.model.EnvLifecycle;
import org.dhorse.api.response.model.EnvReplica;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.repository.po.AffinityTolerationPO;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.model.DockerConfigJson;
import org.dhorse.infrastructure.strategy.cluster.model.DockerConfigJson.Auth;
import org.dhorse.infrastructure.strategy.cluster.model.Replica;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.dhorse.infrastructure.utils.DeploymentThreadPoolUtils;
import org.dhorse.infrastructure.utils.HttpUtils;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.kubernetes.client.Copy;
import io.kubernetes.client.Exec;
import io.kubernetes.client.KubernetesConstants;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.PodMetricsList;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.AutoscalingV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.apis.VersionApi;
import io.kubernetes.client.openapi.models.V1Affinity;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1ContainerStateTerminated;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1CrossVersionObjectReference;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1DaemonSetList;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1DeploymentStrategy;
import io.kubernetes.client.openapi.models.V1EmptyDirVolumeSource;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscaler;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscalerList;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscalerSpec;
import io.kubernetes.client.openapi.models.V1HostPathVolumeSource;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressBackend;
import io.kubernetes.client.openapi.models.V1IngressList;
import io.kubernetes.client.openapi.models.V1IngressRule;
import io.kubernetes.client.openapi.models.V1IngressServiceBackend;
import io.kubernetes.client.openapi.models.V1IngressSpec;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1LabelSelectorRequirement;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1LifecycleHandler;
import io.kubernetes.client.openapi.models.V1LocalObjectReference;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1NodeAffinity;
import io.kubernetes.client.openapi.models.V1NodeSelector;
import io.kubernetes.client.openapi.models.V1NodeSelectorRequirement;
import io.kubernetes.client.openapi.models.V1NodeSelectorTerm;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodAffinity;
import io.kubernetes.client.openapi.models.V1PodAffinityTerm;
import io.kubernetes.client.openapi.models.V1PodAntiAffinity;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1PreferredSchedulingTerm;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1RollingUpdateDeployment;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceBackendPort;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.openapi.models.V1TCPSocketAction;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.openapi.models.V1WeightedPodAffinityTerm;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;

public class K8sClusterStrategy implements ClusterStrategy {

	private static final Logger logger = LoggerFactory.getLogger(K8sClusterStrategy.class);

	@Override
	public Void createSecret(ClusterPO clusterPO, ImageRepo imageRepo) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		V1Secret secret = new V1Secret();
		secret.setApiVersion("v1");
		secret.setKind("Secret");
		secret.setType("kubernetes.io/dockerconfigjson");
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(K8sUtils.DOCKER_REGISTRY_KEY);
		meta.setLabels(Collections.singletonMap("app", K8sUtils.DOCKER_REGISTRY_KEY));
		secret.setMetadata(meta);
		secret.setData(dockerConfigData(imageRepo));
		
		try {
			V1NamespaceList namespaceList = coreApi.listNamespace(null, null, null, null, null, null, null, null, null, null);
			if(CollectionUtils.isEmpty(namespaceList.getItems())) {
				return null;
			}
			for(V1Namespace n : namespaceList.getItems()) {
				String namespace = n.getMetadata().getName();
				if(!K8sUtils.DHORSE_NAMESPACE.equals(namespace)
						&& K8sUtils.getSystemNamspaces().contains(namespace)) {
					continue;
				}
				if(!"Active".equals(n.getStatus().getPhase())){
					continue;
				}
				V1SecretList secretList = coreApi.listNamespacedSecret(namespace, null, null, null, null,
						"app=" + K8sUtils.DOCKER_REGISTRY_KEY, null, null, null, null, null);
				if(CollectionUtils.isEmpty(secretList.getItems())) {
					coreApi.createNamespacedSecret(namespace, secret, null, null, null, null);
				}else {
					coreApi.replaceNamespacedSecret(K8sUtils.DOCKER_REGISTRY_KEY, namespace, secret, null, null, null, null);
				}
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.IMAGE_REPO_AUTH_FAILURE);
		}
		return null;
	}
	
	private Map<String, byte[]> dockerConfigData(ImageRepo imageRepo) {
		Map<String, byte[]> data = new HashMap<>();
		Encoder encoder = Base64.getEncoder();
		String auth = encoder.encodeToString((imageRepo.getAuthName() + ":" + imageRepo.getAuthPassword()).getBytes());
		Auth auths = new Auth();
		auths.setUsername(imageRepo.getAuthName());
		auths.setPassword(imageRepo.getAuthPassword());
		auths.setAuth(auth);
		DockerConfigJson dockerConfigJson = new DockerConfigJson();
		dockerConfigJson.setAuths(Collections.singletonMap(imageRepo.getUrl(), auths));
		data.put(".dockerconfigjson", JsonUtils.toJsonString(dockerConfigJson).getBytes());
		return data;
	}
	
	@Override
	public Replica readDeployment(ClusterPO clusterPO, AppEnv appEnv, AppPO appPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		AppsV1Api api = new AppsV1Api(apiClient);
		String namespace = appEnv.getNamespaceName();
		String labelSelector = K8sUtils.getDeploymentLabelSelector(appPO.getAppName(), appEnv.getTag());
		try {
			V1DeploymentList deployment = api.listNamespacedDeployment(namespace, null, null, null, null,
					labelSelector, null, null, null, null, null);
			if(deployment == null || CollectionUtils.isEmpty(deployment.getItems())) {
				return null;
			}
			V1PodSpec podSpec = deployment.getItems().get(0).getSpec().getTemplate().getSpec();
			Replica replica = new Replica();
			replica.setImageName(imageName(appPO, podSpec));
			return replica;
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_DEPLOYMENT_FAILURE);
		}
		return null;
	}
	
	public boolean createDeployment(DeploymentContext context) {
		logger.info("Start to deploy k8s server");
		V1Deployment deployment = new V1Deployment();
		deployment.apiVersion("apps/v1");
		deployment.setKind("Deployment");
		deployment.setMetadata(deploymentMetaData(context.getDeploymentName(), context.getAppEnv().getTag()));
		deployment.setSpec(deploymentSpec(context));
		ApiClient apiClient = this.apiClient(context.getCluster().getClusterUrl(),
				context.getCluster().getAuthToken());
		AppsV1Api api = new AppsV1Api(apiClient);
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		String namespace = context.getAppEnv().getNamespaceName();
		String labelSelector = K8sUtils.getDeploymentLabelSelector(context.getDeploymentName());
		try {
			V1DeploymentList oldDeployment = api.listNamespacedDeployment(namespace, null, null, null, null,
					labelSelector, null, null, null, null, null);
			if (CollectionUtils.isEmpty(oldDeployment.getItems())) {
				deployment = api.createNamespacedDeployment(namespace, deployment, null, null, null, null);
			} else {
				deployment = api.replaceNamespacedDeployment(context.getDeploymentName(), namespace, deployment, null, null,
						null, null);
			}
			
			// 自动扩容任务
			createAutoScaling(context.getAppEnv(), context.getDeploymentName(), apiClient);

			if(!checkHealthOfAll(coreApi, namespace, labelSelector)) {
				logger.warn("Failed to create k8s deployment, because the replica is not fully started");
				return false;
			}
			
			//部署service
			createService(context);
			
			//Node应用的ingress
			createIngress(context);
			
		} catch (ApiException e) {
			if (!StringUtils.isBlank(e.getMessage())) {
				logger.error("Failed to create k8s deployment, message: {}", e.getMessage());
			} else {
				logger.error("Failed to create k8s deployment, message: {}", e.getResponseBody());
			}
			return false;
		}
		logger.info("End to deploy k8s server");
		return true;
	}
	
	private boolean createService(DeploymentContext context) throws ApiException {
		logger.info("Start to create service");
		ApiClient apiClient = this.apiClient(context.getCluster().getClusterUrl(),
				context.getCluster().getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		String namespace = context.getAppEnv().getNamespaceName();
		String serviceName = K8sUtils.getServiceName(context.getApp().getAppName(), context.getAppEnv().getTag());
		V1ServiceList serviceList = coreApi.listNamespacedService(namespace, null, null, null, null,
				"app=" + serviceName, 1, null, null, null, null);
		V1Service service = null;
		if (CollectionUtils.isEmpty(serviceList.getItems())) {
			service = new V1Service();
			service.apiVersion("v1");
			service.setKind("Service");
			service.setMetadata(serviceMeta(serviceName));
			service.setSpec(serviceSpec(context));
			service = coreApi.createNamespacedService(namespace, service, null, null, null, null);
		} else {
			service = serviceList.getItems().get(0);
			service.getSpec().setPorts(servicePorts(context));
			service = coreApi.replaceNamespacedService(serviceName, namespace, service, null, null, null, null);
		}
		logger.info("End to create service");
		return true;
	}
	
	private boolean createIngress(DeploymentContext context) throws ApiException {
		if(!this.nodeApp(context.getApp())) {
			return true;
		}
		if(StringUtils.isBlank(context.getAppEnv().getExt())){
			return true;
		}
		logger.info("Start to create ingress");
		ApiClient apiClient = this.apiClient(context.getCluster().getClusterUrl(),
				context.getCluster().getAuthToken());
		NetworkingV1Api networkingApi = new NetworkingV1Api(apiClient);
		String namespace = context.getAppEnv().getNamespaceName();
		String serviceName = K8sUtils.getServiceName(context.getApp().getAppName(), context.getAppEnv().getTag());
		V1IngressList list = networkingApi.listNamespacedIngress(namespace, null, null, null, null,
				"app=" + serviceName, 1, null, null, null, null);
		V1Ingress ingress = null;
		if (CollectionUtils.isEmpty(list.getItems())) {
			ingress = new V1Ingress();
			ingress.apiVersion("networking.k8s.io/v1");
			ingress.setKind("Ingress");
			ingress.setMetadata(ingressMeta(serviceName));
			ingress.setSpec(ingressSpec(context));
			ingress = networkingApi.createNamespacedIngress(namespace, ingress, null, null, null, null);
		} else {
			ingress = list.getItems().get(0);
			ingress.setSpec(ingressSpec(context));
			ingress = networkingApi.replaceNamespacedIngress(serviceName, namespace, ingress, null, null, null, null);
		}
		logger.info("End to create ingress");
		return true;
	}
	
	private V1ObjectMeta ingressMeta(String appName) {
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(appName);
		metadata.setLabels(Collections.singletonMap("app", appName));
		return metadata;
	}
	
	private V1IngressSpec ingressSpec(DeploymentContext context) {
		V1ServiceBackendPort serviceBackendPort = new V1ServiceBackendPort();
		serviceBackendPort.setNumber(context.getAppEnv().getServicePort());
		V1IngressServiceBackend ingressServiceBackend = new V1IngressServiceBackend();
		ingressServiceBackend.setName(K8sUtils.getServiceName(context.getApp().getAppName(), context.getAppEnv().getTag()));
		ingressServiceBackend.setPort(serviceBackendPort);
		V1IngressBackend ingressBackend = new V1IngressBackend();
		ingressBackend.setService(ingressServiceBackend);
		
		V1IngressRule ingressRule = new V1IngressRule();
		ingressRule.setHost(((EnvExtendNode)context.getEnvExtend()).getIngressHost());
		
		V1IngressSpec spec = new V1IngressSpec();
		spec.setDefaultBackend(ingressBackend);
		spec.setIngressClassName("nginx");
		spec.setRules(Arrays.asList(ingressRule));
		return spec;
	}
	
	public boolean deleteDeployment(ClusterPO clusterPO, AppPO appPO, AppEnvPO appEnvPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		AppsV1Api api = new AppsV1Api(apiClient);
		String namespace = appEnvPO.getNamespaceName();
		String depolymentName = K8sUtils.getDeploymentName(appPO.getAppName(), appEnvPO.getTag());
		String labelSelector = K8sUtils.getDeploymentLabelSelector(depolymentName);
		try {
			V1DeploymentList oldDeployment = api.listNamespacedDeployment(namespace, null, null, null, null,
					labelSelector, null, null, null, null, null);
			if (!CollectionUtils.isEmpty(oldDeployment.getItems())) {
				V1Status status = api.deleteNamespacedDeployment(depolymentName, namespace, null, null, null, null, null, null);
				if(status == null || !KubernetesConstants.V1STATUS_SUCCESS.equals(status.getStatus())){
					return false;
				}
			}
			if(!deleteAutoScaling(namespace, depolymentName, apiClient)) {
				return false;
			}
			String serviceName = K8sUtils.getServiceName(appPO.getAppName(), appEnvPO.getTag());
			if(!deleteService(namespace, serviceName, apiClient)) {
				return false;
			}
			if(!deleteIngress(namespace, serviceName, apiClient)) {
				return false;
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DEPLOYMENT_DELETED_FAILURE);
		}
		
		return true;
	}

	public boolean autoScaling(AppPO appPO, AppEnvPO appEnvPO, ClusterPO clusterPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		String deploymentName = K8sUtils.getReplicaAppName(appPO.getAppName(), appEnvPO.getTag());
		return createAutoScaling(appEnvPO, deploymentName, apiClient);
	}

	private boolean createAutoScaling(AppEnvPO appEnvPO, String deploymentName, ApiClient apiClient) {
		AutoscalingV1Api autoscalingApi = new AutoscalingV1Api(apiClient);
		V1HorizontalPodAutoscaler body = new V1HorizontalPodAutoscaler();
		body.setKind("HorizontalPodAutoscaler");
		body.setApiVersion("autoscaling/v1");
		V1HorizontalPodAutoscalerSpec spec = new V1HorizontalPodAutoscalerSpec();
		spec.setMinReplicas(appEnvPO.getMinReplicas());
		spec.setMaxReplicas(appEnvPO.getMaxReplicas());
		V1CrossVersionObjectReference scaleTargetRef = new V1CrossVersionObjectReference();
		scaleTargetRef.setApiVersion("apps/v1");
		scaleTargetRef.setKind("Deployment");
		scaleTargetRef.setName(deploymentName);
		spec.setScaleTargetRef(scaleTargetRef);
		spec.setTargetCPUUtilizationPercentage(appEnvPO.getAutoScalingCpu());
		body.setMetadata(deploymentMetaData(deploymentName, appEnvPO.getTag()));
		body.setSpec(spec);
		String labelSelector = K8sUtils.getDeploymentLabelSelector(deploymentName);
		try {
			V1HorizontalPodAutoscalerList autoscalerList = autoscalingApi.listNamespacedHorizontalPodAutoscaler(
					appEnvPO.getNamespaceName(), null, null, null, null, labelSelector, 1, null, null, null,
					null);
			if (CollectionUtils.isEmpty(autoscalerList.getItems())) {
				autoscalingApi.createNamespacedHorizontalPodAutoscaler(appEnvPO.getNamespaceName(), body, null,
						null, null, null);
			} else {
				autoscalingApi.replaceNamespacedHorizontalPodAutoscaler(deploymentName, appEnvPO.getNamespaceName(),
						body, null, null, null, null);
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.REPLICA_RESTARTED_FAILURE);
		}
		return true;
	}
	
	private boolean deleteAutoScaling(String namespace, String deploymentName, ApiClient apiClient) {
		AutoscalingV1Api autoscalingApi = new AutoscalingV1Api(apiClient);
		String labelSelector = K8sUtils.getDeploymentLabelSelector(deploymentName);
		try {
			V1HorizontalPodAutoscalerList autoscalerList = autoscalingApi.listNamespacedHorizontalPodAutoscaler(
					namespace, null, null, null, null, labelSelector, 1, null, null, null, null);
			if (CollectionUtils.isEmpty(autoscalerList.getItems())) {
				return true;
			}
			V1Status status = autoscalingApi.deleteNamespacedHorizontalPodAutoscaler(deploymentName,
					namespace, null, null, null, null, null, null);
			if(status == null || !KubernetesConstants.V1STATUS_SUCCESS.equals(status.getStatus())){
				return false;
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DELETE_AUTO_SCALING_FAILURE);
		}
		return true;
	}
	
	/**
	 * coreApi.deleteNamespacedService方法返回值会报json转化异常，
	 * 从返回结果来看应该是V1Status对象，而非V1Service对象，
	 * 这可能是client版本的bug。
	 * 所以该方法会暂时忽略抛出的异常。
	 */
	private boolean deleteService(String namespace, String serviceName, ApiClient apiClient) {
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		try {
			V1ServiceList serviceList = coreApi.listNamespacedService(namespace, null, null, null, null,
					"app=" + serviceName, 1, null, null, null, null);
			if (CollectionUtils.isEmpty(serviceList.getItems())) {
				return true;
			}
			V1Service service = coreApi.deleteNamespacedService(serviceName,
					namespace, null, null, null, null, null, null);
			if(service == null || !KubernetesConstants.V1STATUS_SUCCESS.equals(service.getStatus().getConditions().get(0).getStatus())){
				return false;
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DELETE_AUTO_SCALING_FAILURE);
		} catch (Exception e) {
			//LogUtils.throwException(logger, message, MessageCodeEnum.DELETE_AUTO_SCALING_FAILURE);
			//暂时忽略抛出的异常
			logger.error("Faile to delete service", e);
		}
		return true;
	}
	
	private boolean deleteIngress(String namespace, String serviceName, ApiClient apiClient) {
		NetworkingV1Api networkingApi = new NetworkingV1Api(apiClient);
		try {
			V1IngressList list = networkingApi.listNamespacedIngress(namespace, null, null, null, null,
					"app=" + serviceName, 1, null, null, null, null);
			if (CollectionUtils.isEmpty(list.getItems())) {
				return true;
			}
			V1Status status = networkingApi.deleteNamespacedIngress(serviceName,
					namespace, null, null, null, null, null, null);
			if(status == null || !KubernetesConstants.V1STATUS_SUCCESS.equals(status.getStatus())){
				return false;
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DELETE_AUTO_SCALING_FAILURE);
		}
		return true;
	}

	private V1ObjectMeta serviceMeta(String appName) {
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(appName);
		metadata.setLabels(Collections.singletonMap("app", appName));
		return metadata;
	}
	
	private V1ServiceSpec serviceSpec(DeploymentContext context) {
		V1ServiceSpec spec = new V1ServiceSpec();
		spec.setSelector(Collections.singletonMap("app", context.getDeploymentName()));
		spec.setPorts(servicePorts(context));
		spec.setType("ClusterIP");
		return spec;
	}
	
	private List<V1ServicePort> servicePorts(DeploymentContext context){
		//主端口
		V1ServicePort servicePort = new V1ServicePort();
		servicePort.setName("major");
		servicePort.setProtocol("TCP");
		servicePort.setPort(context.getAppEnv().getServicePort());
		servicePort.setTargetPort(new IntOrString(context.getAppEnv().getServicePort()));
		List<V1ServicePort> ports = new ArrayList<>();
		ports.add(servicePort);
		
		//辅助端口
		if(!StringUtils.isBlank(context.getAppEnv().getMinorPorts())) {
			String[] portStr = context.getAppEnv().getMinorPorts().split(",");
			for(int i = 0; i < portStr.length; i++) {
				V1ServicePort one = new V1ServicePort();
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
	
	private V1ObjectMeta deploymentMetaData(String appName, String envTag) {
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(appName);
		Map<String, String> labelMap = new HashMap<>();
		labelMap.put("app", appName);
		labelMap.put("deployer", K8sUtils.getDhorseLabelSelector(envTag));
		metadata.setLabels(labelMap);
		return metadata;
	}

	private V1DeploymentSpec deploymentSpec(DeploymentContext context) {
		V1DeploymentSpec spec = new V1DeploymentSpec();
		spec.setReplicas(context.getAppEnv().getMinReplicas());
		spec.setSelector(specSelector(context.getDeploymentName()));
		spec.setTemplate(specTemplate(context));
		spec.setStrategy(deploymentStrategy());
		return spec;
	}

	private V1LabelSelector specSelector(String deploymentName) {
		V1LabelSelector selector = new V1LabelSelector();
		selector.setMatchLabels(Collections.singletonMap("app", deploymentName));
		return selector;
	}
	
	private V1PodTemplateSpec specTemplate(DeploymentContext context) {
		Map<String, String> labels = new HashMap<>();
		labels.put("app", context.getDeploymentName());
		labels.put("deployer", K8sUtils.getDhorseLabelSelector(context.getAppEnv().getTag()));
		labels.put("version", String.valueOf(System.currentTimeMillis()));
		V1ObjectMeta specMetadata = new V1ObjectMeta();
		specMetadata.setLabels(labels);
		V1PodTemplateSpec template = new V1PodTemplateSpec();
		template.setMetadata(specMetadata);
		template.setSpec(podSpec(context));
		return template;
	}

	private V1DeploymentStrategy deploymentStrategy() {
		IntOrString size = new IntOrString("25%");
		V1RollingUpdateDeployment ru = new V1RollingUpdateDeployment();
		ru.setMaxSurge(size);
		ru.maxUnavailable(size);
		
		V1DeploymentStrategy s = new V1DeploymentStrategy();
		s.setRollingUpdate(ru);
		return s;
	}
	
	private V1PodSpec podSpec(DeploymentContext context) {
		V1PodSpec podSpec = new V1PodSpec();
		podSpec.setInitContainers(initContainer(context));
		podSpec.setContainers(containers(context));
		V1LocalObjectReference r = new V1LocalObjectReference();
		r.setName(K8sUtils.DOCKER_REGISTRY_KEY);
		podSpec.setImagePullSecrets(Arrays.asList(r));
		podSpec.setAffinity(affinity(context));
		podSpec.setTolerations(toleration(context));
		podSpec.setVolumes(volumes(context));
		return podSpec;
	}
	
	private V1Affinity affinity(DeploymentContext context) {
		V1Affinity affinity = new V1Affinity();
		affinity.setNodeAffinity(nodeAffinity(context));
		affinity.setPodAffinity(podAffinity(context));
		affinityApp(affinity, context);
		affinity.setPodAntiAffinity(podAntiAffinity(context));
		return affinity;
	}
	
	private V1NodeAffinity nodeAffinity(DeploymentContext context) {
		List<AffinityTolerationPO> nodeAffinitys = context.getAffinitys().stream()
				.filter(e -> SchedulingTypeEnum.NODE_AFFINITY.getCode().equals(e.getSchedulingType()))
				.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(nodeAffinitys)) {
			return null;
		}
		
		V1NodeAffinity nodeAffinity = new V1NodeAffinity();
		
		//1.硬亲和
		List<AffinityTolerationPO> affinityForce = nodeAffinitys.stream()
				.filter(e -> AffinityLevelEnum.FORCE_AFFINITY.getCode().equals(e.getAffinityLevel()))
				.collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(affinityForce)) {
			List<V1NodeSelectorRequirement> requirements = new ArrayList<>();
			for(AffinityTolerationPO affintiy : affinityForce) {
				V1NodeSelectorRequirement requirement = new V1NodeSelectorRequirement();
				requirement.setKey(affintiy.getKeyName());
				requirement.setOperator(affintiy.getOperator());
				if(!StringUtils.isBlank(affintiy.getValueList())) {
					requirement.setValues(Arrays.asList(affintiy.getValueList().split(",")));
				}
				requirements.add(requirement);
			}
			V1NodeSelectorTerm nodeSelectorTerm = new V1NodeSelectorTerm();
			nodeSelectorTerm.setMatchExpressions(requirements);
			V1NodeSelector nodeSelector = new V1NodeSelector();
			nodeSelector.setNodeSelectorTerms(Arrays.asList(nodeSelectorTerm));
			
			nodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(nodeSelector);
		}
		
		//2.软亲和
		List<AffinityTolerationPO> affinitySoft = nodeAffinitys.stream()
				.filter(e -> AffinityLevelEnum.SOFT_AFFINITY.getCode().equals(e.getAffinityLevel()))
				.collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(affinitySoft)) {
			List<V1PreferredSchedulingTerm> schedulingTerms = new ArrayList<>();
			for(AffinityTolerationPO affintiy : affinitySoft) {
				V1NodeSelectorRequirement requirement = new V1NodeSelectorRequirement();
				requirement.setKey(affintiy.getKeyName());
				requirement.setOperator(affintiy.getOperator());
				if(!StringUtils.isBlank(affintiy.getValueList())) {
					requirement.setValues(Arrays.asList(affintiy.getValueList().split(",")));
				}
				
				V1NodeSelectorTerm nodeSelectorTerm = new V1NodeSelectorTerm();
				nodeSelectorTerm.setMatchExpressions(Arrays.asList(requirement));
				
				V1NodeSelector nodeSelector = new V1NodeSelector();
				nodeSelector.setNodeSelectorTerms(Arrays.asList(nodeSelectorTerm));
				
				V1PreferredSchedulingTerm schedulingTerm = new V1PreferredSchedulingTerm();
				schedulingTerm.setPreference(nodeSelectorTerm);
				schedulingTerm.setWeight(Integer.valueOf(affintiy.getWeight()));
				schedulingTerms.add(schedulingTerm);
			}
			
			nodeAffinity.setPreferredDuringSchedulingIgnoredDuringExecution(schedulingTerms);
		}
		
		return nodeAffinity;
	}
	
	private V1PodAffinity podAffinity(DeploymentContext context) {

		List<AffinityTolerationPO> affinitys = context.getAffinitys().stream()
				.filter(e -> SchedulingTypeEnum.REPLICA_AFFINITY.getCode().equals(e.getSchedulingType()))
				.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(affinitys)) {
			return null;
		}
		
		V1PodAffinity affinity = new V1PodAffinity();
		
		//1.硬亲和
		List<AffinityTolerationPO> affinityForce = affinitys.stream()
				.filter(e -> AffinityLevelEnum.FORCE_AFFINITY.getCode().equals(e.getAffinityLevel()))
				.collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(affinityForce)) {
			List<V1PodAffinityTerm> affinityTerms = new ArrayList<>();
			for(AffinityTolerationPO affintiy : affinityForce) {
				V1LabelSelectorRequirement requirement = new V1LabelSelectorRequirement();
				requirement.setKey(affintiy.getKeyName());
				requirement.setOperator(affintiy.getOperator());
				if(!StringUtils.isBlank(affintiy.getValueList())) {
					requirement.setValues(Arrays.asList(affintiy.getValueList().split(",")));
				}
				
				V1LabelSelector labelSelector = new V1LabelSelector();
				labelSelector.setMatchExpressions(Arrays.asList(requirement));
				
				V1PodAffinityTerm nodeSelectorTerm = new V1PodAffinityTerm();
				nodeSelectorTerm.setLabelSelector(labelSelector);
				nodeSelectorTerm.setTopologyKey(topologyKey(affintiy.getTopologyKey()));
				affinityTerms.add(nodeSelectorTerm);
			}
			
			affinity.setRequiredDuringSchedulingIgnoredDuringExecution(affinityTerms);
		}
		
		//2.软亲和
		List<AffinityTolerationPO> affinitySoft = affinitys.stream()
				.filter(e -> AffinityLevelEnum.SOFT_AFFINITY.getCode().equals(e.getAffinityLevel()))
				.collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(affinitySoft)) {
			List<V1WeightedPodAffinityTerm> weightedTerms = new ArrayList<>();
			for(AffinityTolerationPO affintiy : affinitySoft) {
				V1LabelSelectorRequirement sr = new V1LabelSelectorRequirement();
				sr.setKey(affintiy.getKeyName());
				sr.setOperator(affintiy.getOperator());
				if(!StringUtils.isBlank(affintiy.getValueList())) {
					sr.setValues(Arrays.asList(affintiy.getValueList().split(",")));
				}
				
				V1LabelSelector labelSelector = new V1LabelSelector();
				labelSelector.setMatchExpressions(Arrays.asList(sr));
				
				V1PodAffinityTerm podAffinityTerm = new V1PodAffinityTerm();
				podAffinityTerm.setLabelSelector(labelSelector);
				podAffinityTerm.setTopologyKey(topologyKey(affintiy.getTopologyKey()));
				
				V1WeightedPodAffinityTerm weightedTerm = new V1WeightedPodAffinityTerm();
				weightedTerm.setPodAffinityTerm(podAffinityTerm);
				weightedTerm.setWeight(Integer.valueOf(affintiy.getWeight()));
				weightedTerms.add(weightedTerm);
			}
			
			affinity.setPreferredDuringSchedulingIgnoredDuringExecution(weightedTerms);
		}
		
		return affinity;
	
	}
	
	private String topologyKey(String topologyKey) {
		return StringUtils.isBlank(topologyKey) ? K8sUtils.DEFAULT_TOPOLOGY_KEY : topologyKey;
	}
	
	private V1PodAntiAffinity podAntiAffinity(DeploymentContext context) {

		List<AffinityTolerationPO> affinitys = context.getAffinitys().stream()
				.filter(e -> SchedulingTypeEnum.REPLICA_ANTIAFFINITY.getCode().equals(e.getSchedulingType()))
				.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(affinitys)) {
			return null;
		}
		
		V1PodAntiAffinity affinity = new V1PodAntiAffinity();
		
		//1.硬亲和
		List<AffinityTolerationPO> affinityForce = affinitys.stream()
				.filter(e -> AffinityLevelEnum.FORCE_AFFINITY.getCode().equals(e.getAffinityLevel()))
				.collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(affinityForce)) {
			List<V1PodAffinityTerm> affinityTerms = new ArrayList<>();
			for(AffinityTolerationPO affintiy : affinityForce) {
				V1LabelSelectorRequirement requirement = new V1LabelSelectorRequirement();
				requirement.setKey(affintiy.getKeyName());
				requirement.setOperator(affintiy.getOperator());
				if(!StringUtils.isBlank(affintiy.getValueList())) {
					requirement.setValues(Arrays.asList(affintiy.getValueList().split(",")));
				}
				
				V1LabelSelector labelSelector = new V1LabelSelector();
				labelSelector.setMatchExpressions(Arrays.asList(requirement));
				
				V1PodAffinityTerm nodeSelectorTerm = new V1PodAffinityTerm();
				nodeSelectorTerm.setLabelSelector(labelSelector);
				nodeSelectorTerm.setTopologyKey(topologyKey(affintiy.getTopologyKey()));
				affinityTerms.add(nodeSelectorTerm);
			}
			
			affinity.setRequiredDuringSchedulingIgnoredDuringExecution(affinityTerms);
		}
		
		//2.软亲和
		List<AffinityTolerationPO> affinitySoft = affinitys.stream()
				.filter(e -> AffinityLevelEnum.SOFT_AFFINITY.getCode().equals(e.getAffinityLevel()))
				.collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(affinitySoft)) {
			List<V1WeightedPodAffinityTerm> weightedTerms = new ArrayList<>();
			for(AffinityTolerationPO affintiy : affinitySoft) {
				V1LabelSelectorRequirement sr = new V1LabelSelectorRequirement();
				sr.setKey(affintiy.getKeyName());
				sr.setOperator(affintiy.getOperator());
				if(!StringUtils.isBlank(affintiy.getValueList())) {
					sr.setValues(Arrays.asList(affintiy.getValueList().split(",")));
				}
				
				V1LabelSelector labelSelector = new V1LabelSelector();
				labelSelector.setMatchExpressions(Arrays.asList(sr));
				
				V1PodAffinityTerm podAffinityTerm = new V1PodAffinityTerm();
				podAffinityTerm.setLabelSelector(labelSelector);
				podAffinityTerm.setTopologyKey(topologyKey(affintiy.getTopologyKey()));
				
				V1WeightedPodAffinityTerm weightedTerm = new V1WeightedPodAffinityTerm();
				weightedTerm.setPodAffinityTerm(podAffinityTerm);
				weightedTerm.setWeight(Integer.valueOf(affintiy.getWeight()));
				weightedTerms.add(weightedTerm);
			}
			
			affinity.setPreferredDuringSchedulingIgnoredDuringExecution(weightedTerms);
		}
		
		return affinity;
	
	}
	
	private void affinityApp(V1Affinity affinity, DeploymentContext context) {
		List<String> affinityNames = context.getApp().getAffinityAppNames();
		if(CollectionUtils.isEmpty(affinityNames)) {
			return;
		}
		
		List<String> affinityValues = new ArrayList<>();
		for(String appName : affinityNames) {
			affinityValues.add(K8sUtils.getDeploymentName(appName, context.getAppEnv().getTag()));
		}
		
		V1LabelSelectorRequirement sr = new V1LabelSelectorRequirement();
		sr.setKey(K8sUtils.APP_KEY);
		sr.setOperator("In");
		sr.setValues(affinityValues);
		
		V1LabelSelector labelSelector = new V1LabelSelector();
		labelSelector.setMatchExpressions(Arrays.asList(sr));
		
		V1PodAffinityTerm podAffinityTerm = new V1PodAffinityTerm();
		podAffinityTerm.setLabelSelector(labelSelector);
		podAffinityTerm.setTopologyKey(K8sUtils.DEFAULT_TOPOLOGY_KEY);
		
		V1WeightedPodAffinityTerm weightedTerm = new V1WeightedPodAffinityTerm();
		weightedTerm.setWeight(100);
		weightedTerm.setPodAffinityTerm(podAffinityTerm);
		
		V1PodAffinity podAffinity = affinity.getPodAffinity();
		if(podAffinity == null) {
			podAffinity = new V1PodAffinity();
			affinity.setPodAffinity(podAffinity);
		}
		List<V1WeightedPodAffinityTerm> terms = podAffinity.getPreferredDuringSchedulingIgnoredDuringExecution();
		if(CollectionUtils.isEmpty(terms)) {
			podAffinity.setPreferredDuringSchedulingIgnoredDuringExecution(Arrays.asList(weightedTerm));
		}else {
			terms.add(weightedTerm);
		}
	}
	
	private List<V1Toleration> toleration(DeploymentContext context) {
		List<AffinityTolerationPO> configs = context.getAffinitys().stream()
				.filter(e -> SchedulingTypeEnum.NODE_TOLERATION.getCode().equals(e.getSchedulingType()))
				.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(configs)) {
			return null;
		}
		
		List<V1Toleration> tolerations = new ArrayList<>();
		for(AffinityTolerationPO c : configs) {
			V1Toleration t = new V1Toleration();
			t.setKey(c.getKeyName());
			t.setOperator(c.getOperator());
			t.setValue(c.getValueList());
			t.setEffect(c.getEffectType());
			t.setTolerationSeconds(StringUtils.isEmpty(c.getDuration()) ? null : Long.valueOf(c.getDuration()));
			tolerations.add(t);
		}
		
		return tolerations;
	}
	
	private List<V1Container> containers(DeploymentContext context) {
		AppEnvPO appEnvPO = context.getAppEnv();
		V1Container container = new V1Container();
		container.setName(context.getDeploymentName());
		containerOfJar(context, container);
		containerOfNode(context, container);
		envVars(context, container);
		container.setImagePullPolicy("Always");
		
		//主端口
		V1ContainerPort servicePort = new V1ContainerPort();
		servicePort.setName("major");
		servicePort.setContainerPort(appEnvPO.getServicePort());
		List<V1ContainerPort> ports = new ArrayList<>();
		ports.add(servicePort);
		
		//辅助端口
		if(!StringUtils.isBlank(appEnvPO.getMinorPorts())) {
			String[] portStr = appEnvPO.getMinorPorts().split(",");
			for(int i = 0; i < portStr.length; i++) {
				V1ContainerPort containerPort = new V1ContainerPort();
				containerPort.setName("minor" + (i + 1));
				containerPort.setContainerPort(Integer.valueOf(portStr[i]));
				ports.add(containerPort);
			}
		}
		
		container.setPorts(ports);
		
		// 设置资源
		Quantity cpu = new Quantity(new BigDecimal(appEnvPO.getReplicaCpu()).movePointLeft(3).toPlainString());
		Quantity memory = new Quantity(appEnvPO.getReplicaMemory() + "Mi");
		Map<String, Quantity> requests = new HashMap<>();
		requests.put("cpu", cpu);
		requests.put("memory", memory);
		Map<String, Quantity> limits = new HashMap<>();
		limits.put("cpu", cpu);
		limits.put("memory", memory);
		V1ResourceRequirements resources = new V1ResourceRequirements();
		resources.setRequests(requests);
		resources.setLimits(limits);
		container.setResources(resources);
		container.setVolumeMounts(volumeMounts(context));
		probe(container, context);
		lifecycle(container, context);
		
		return Arrays.asList(container);
	}
	
	private void containerOfJar(DeploymentContext context, V1Container container) {
		if(!jarFileType(context.getApp())) {
			return;
		}
		container.setImage(context.getFullNameOfImage());
		commandsOfJar(container, context);
		argsOfJar(container, context);
	}
	
	private void containerOfNode(DeploymentContext context, V1Container container) {
		if(!TechTypeEnum.VUE.getCode().equals(context.getApp().getTechType())
				&& !TechTypeEnum.REACT.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		container.setImage(nginxImage(context));
	}
	
	private void envVars(DeploymentContext context, V1Container container) {
		List<V1EnvVar> envVars = new ArrayList<>();
		V1EnvVar envVar = new V1EnvVar();
		envVar.setName("TZ");
		envVar.setValue("Asia/Shanghai");
		envVars.add(envVar);
		container.setEnv(envVars);
		containerOfWar(context, container);
	}
	
	private void containerOfWar(DeploymentContext context, V1Container container) {
		if(!warFileType(context.getApp())) {
			return;
		}
		
		container.setImage(context.getApp().getBaseImage());
		
		//DHorse定义的Jvm参数
		StringBuilder argsStr = new StringBuilder();
		List<String> jvmArgsOfDHorse = jvmArgsOfDHorse(context);
		for(String arg : jvmArgsOfDHorse) {
			argsStr.append(" ").append(arg);
		}
		//用户定义的Jvm参数
		EnvExtendSpringBoot envExtend = context.getEnvExtend();
		if(envExtend != null && !StringUtils.isBlank(envExtend.getJvmArgs())) {
			argsStr.append(" ").append(envExtend.getJvmArgs());
		}
		V1EnvVar envVar = new V1EnvVar();
		envVar.setName("JAVA_OPTS");
		envVar.setValue(argsStr.toString());
		container.getEnv().add(envVar);
	}
	
	private void lifecycle(V1Container container, DeploymentContext context) {
		if(context.getEnvLifecycle() == null) {
			return;
		}
		V1Lifecycle lifecycle = new V1Lifecycle();
		lifecycle.setPostStart(lifecycleHandler(context.getEnvLifecycle().getPostStart(), context));
		lifecycle.setPreStop(lifecycleHandler(context.getEnvLifecycle().getPreStop(), context));
		container.setLifecycle(lifecycle);
	}

	private void probe(V1Container container, DeploymentContext context) {
		if(context.getAppEnv().getServicePort() == null) {
			return;
		}
		startupProbe(container, context);
		readinessProbe(container, context);
		livenessProbe(container, context);
	}
	
	private void startupProbe(V1Container container, DeploymentContext context) {
		Item item = null;
		if(context.getEnvHealth() != null) {
			item = context.getEnvHealth().getStartup();
		}
		V1Probe probe = new V1Probe();
		probeAction(probe, item, context);
		probe.setInitialDelaySeconds(item != null && item.getInitialDelay() != null ? item.getInitialDelay() : 6);
		probe.setPeriodSeconds(item != null && item.getPeriod() != null ? item.getPeriod() : 1);
		probe.setTimeoutSeconds(item != null && item.getTimeout() != null ? item.getTimeout() : 1);
		probe.setSuccessThreshold(item != null && item.getSuccessThreshold() != null ? item.getSuccessThreshold() : 1);
		//默认检查300次（300秒）以后仍然没有启动，则重启服务
		probe.setFailureThreshold(item != null && item.getFailureThreshold() != null ? item.getFailureThreshold() : 300);
		container.startupProbe(probe);
	}
	
	private void readinessProbe(V1Container container, DeploymentContext context) {
		Item item = null;
		if(context.getEnvHealth() != null) {
			item = context.getEnvHealth().getReadiness();
		}
		V1Probe probe = new V1Probe();
		probeAction(probe, item, context);
		probe.setInitialDelaySeconds(item != null && item.getInitialDelay() != null ? item.getInitialDelay() : 6);
		probe.setPeriodSeconds(item != null && item.getPeriod() != null ? item.getPeriod() : 5);
		probe.setTimeoutSeconds(item != null && item.getTimeout() != null ? item.getTimeout() : 1);
		probe.setSuccessThreshold(item != null && item.getSuccessThreshold() != null ? item.getSuccessThreshold() : 1);
		probe.setFailureThreshold(item != null && item.getFailureThreshold() != null ? item.getFailureThreshold() : 3);
		container.readinessProbe(probe);
	}
	
	private void livenessProbe(V1Container container, DeploymentContext context) {
		Item item = null;
		if(context.getEnvHealth() != null) {
			item = context.getEnvHealth().getLiveness();
		}
		V1Probe probe = new V1Probe();
		probeAction(probe, item, context);
		probe.setInitialDelaySeconds(item != null && item.getInitialDelay() != null ? item.getInitialDelay() : 30);
		probe.setPeriodSeconds(item != null && item.getPeriod() != null ? item.getPeriod() : 5);
		probe.setTimeoutSeconds(item != null && item.getTimeout() != null ? item.getTimeout() : 1);
		probe.setSuccessThreshold(item != null && item.getSuccessThreshold() != null ? item.getSuccessThreshold() : 1);
		probe.setFailureThreshold(item != null && item.getFailureThreshold() != null ? item.getFailureThreshold() : 3);
		container.setLivenessProbe(probe);
	}
	
	private void probeAction(V1Probe probe, EnvHealth.Item item, DeploymentContext context) {
		if(item == null || item.getActionType() == null) {
			V1TCPSocketAction action = new V1TCPSocketAction();
			action.setPort(new IntOrString(context.getAppEnv().getServicePort()));
			probe.setTcpSocket(action);
			return;
		}
		if(ActionTypeEnum.HTTP_GET.getCode().equals(item.getActionType())){
			V1HTTPGetAction action = new V1HTTPGetAction();
			action.setPath(item.getAction());
			action.setPort(new IntOrString(context.getAppEnv().getServicePort()));
			probe.setHttpGet(action);
			return;
		}
		if(ActionTypeEnum.TCP.getCode().equals(item.getActionType())) {
			V1TCPSocketAction action = new V1TCPSocketAction();
			action.setPort(new IntOrString(Integer.parseInt(item.getAction())));
			probe.setTcpSocket(action);
			return;
		}
		if(ActionTypeEnum.EXEC.getCode().equals(item.getActionType())){
			V1ExecAction action = new V1ExecAction();
			action.setCommand(Arrays.asList("/bin/sh", "-c", item.getAction()));
			probe.setExec(action);
		}
	}
	
	private V1LifecycleHandler lifecycleHandler(EnvLifecycle.Item item, DeploymentContext context) {
		if(item == null || item.getActionType() == null) {
			return null;
		}
		V1LifecycleHandler handler = new V1LifecycleHandler();
		if(ActionTypeEnum.HTTP_GET.getCode().equals(item.getActionType())){
			V1HTTPGetAction action = new V1HTTPGetAction();
			action.setPath(item.getAction());
			action.setPort(new IntOrString(context.getAppEnv().getServicePort()));
			handler.setHttpGet(action);
		}else if(ActionTypeEnum.TCP.getCode().equals(item.getActionType())) {
			V1TCPSocketAction action = new V1TCPSocketAction();
			action.setPort(new IntOrString(Integer.parseInt(item.getAction())));
			handler.setTcpSocket(action);
		}else if(ActionTypeEnum.EXEC.getCode().equals(item.getActionType())){
			V1ExecAction action = new V1ExecAction();
			action.setCommand(Arrays.asList("/bin/sh", "-c", item.getAction()));
			handler.setExec(action);
		}
		return handler;
	}
	
	/**
	 *   使用Jib通过Jdk的安装目录构建的Jdk镜像，缺少java命令的执行权限，故首先进行赋权。
	 *   需要执行多条shell指令，因此只能使用sh -c模式。
	 */
	private void commandsOfJar(V1Container container, DeploymentContext context){
		StringBuilder commands = new StringBuilder();
		commands.append("chmod +x $JAVA_HOME/bin/java &&");
		commands.append(" $JAVA_HOME/bin/java");
		//DHorse定义的Jvm参数
		List<String> jvmArgsOfDHorse = jvmArgsOfDHorse(context);
		for(String arg : jvmArgsOfDHorse) {
			commands.append(" ").append(arg);
		}
		//用户自定义Jvm参数
		EnvExtendSpringBoot envExtend = context.getEnvExtend();
		if(envExtend != null && !StringUtils.isBlank(envExtend.getJvmArgs())) {
			String[] jvmArgs = envExtend.getJvmArgs().split("\\s+");
			for (String arg : jvmArgs) {
				commands.append(" ").append(arg);
			}
		}
		commands.append(" ").append("-jar");
		String packageFileType = PackageFileTypeEnum.getByCode(((AppExtendJava)context.getApp().getAppExtend()).getPackageFileType()).getValue();
		commands.append(" ").append(Constants.USR_LOCAL_HOME + context.getApp().getAppName() + "." + packageFileType);
		
		container.setCommand(Arrays.asList("sh", "-c", commands.toString()));
	}
	
	private List<String> jvmArgsOfDHorse(DeploymentContext context) {
		List<String> args = new ArrayList<>();
		args.add("-Duser.timezone=Asia/Shanghai");
		args.add("-Denv=" + context.getAppEnv().getTag());
		
		//dhorse-agent参数
		args.add("-javaagent:"+ K8sUtils.AGENT_VOLUME_PATH +"dhorse-agent.jar");
		
		//skywalking-agent参数
		if(!YesOrNoEnum.YES.getCode().equals(context.getAppEnv().getTraceStatus())) {
			return args;
		}
		TraceTemplate traceTemplate = context.getGlobalConfigAgg().getTraceTemplate(context.getAppEnv().getTraceTemplateId());
		if(traceTemplate == null) {
			LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_IS_EMPTY);
		}
		args.add("-javaagent:"+ K8sUtils.AGENT_VOLUME_PATH +"skywalking-agent/skywalking-agent.jar");
		args.add("-Dskywalking.collector.backend_service=" + traceTemplate.getServiceUrl());
		args.add("-Dskywalking.agent.service_name=" + context.getApp().getAppName());
		return args;
	}
	
	private void argsOfJar(V1Container container, DeploymentContext context){
		List<String> args = new ArrayList<>();
		args.add("--server.port=" + context.getAppEnv().getServicePort());
		container.setArgs(args);
	}
	
	private List<V1Container> initContainer(DeploymentContext context) {
		List<V1Container> containers = new ArrayList<>();
		initContainerOfWar(context, containers);
		initContainerOfTraceAgent(context, containers);
		initContainerOfDHorseAgent(context, containers);
		initContainerOfNode(context, containers);
		return containers;
	}
	
	private String nginxImage(DeploymentContext context) {
		//如：nginx:1.23.3-alpine
		if(ImageSourceEnum.VERSION.getCode().equals(context.getApp().getBaseImageSource())) {
			return "nginx:" + NginxVersionEnum.getByCode(context.getApp().getBaseImageVersion()).getValue() + "-alpine";
		}
		return context.getApp().getBaseImage();
	}
	
	private void initContainerOfTraceAgent(DeploymentContext context, List<V1Container> containers) {
		if(!TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		if(YesOrNoEnum.NO.getCode().equals(context.getAppEnv().getTraceStatus())) {
			return;
		}
		
		TraceTemplate traceTemplate = context.getGlobalConfigAgg().getTraceTemplate(context.getAppEnv().getTraceTemplateId());
		if(traceTemplate == null) {
			LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_IS_EMPTY);
		}
		
		V1Container initContainer = new V1Container();
		initContainer.setName("skywalking-agent");
		initContainer.setImage(context.getFullNameOfTraceAgentImage());
		initContainer.setImagePullPolicy("Always");
		initContainer.setCommand(Arrays.asList("/bin/sh", "-c"));
		initContainer.setArgs(Arrays.asList("cp -rf /skywalking-agent " + K8sUtils.AGENT_VOLUME_PATH));
		
		V1VolumeMount volumeMount = new V1VolumeMount();
		volumeMount.setMountPath(K8sUtils.AGENT_VOLUME_PATH);
		volumeMount.setName(K8sUtils.AGENT_VOLUME);
		initContainer.setVolumeMounts(Arrays.asList(volumeMount));
		containers.add(initContainer);
	}
	
	private void initContainerOfDHorseAgent(DeploymentContext context, List<V1Container> containers) {
		if(!TechTypeEnum.SPRING_BOOT.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		
		if(context.getEnvExtend() == null) {
			return;
		}
		
		if(YesOrNoEnum.NO.getCode().equals(((EnvExtendSpringBoot)context.getEnvExtend()).getJvmMetricsStatus())) {
			return;
		}
		
		V1Container initContainer = new V1Container();
		initContainer.setName("dhorse-agent");
		initContainer.setImage(context.getFullNameOfDHorseAgentImage());
		initContainer.setImagePullPolicy("Always");
		initContainer.setCommand(Arrays.asList("/bin/sh", "-c"));
		initContainer.setArgs(Arrays.asList("cp -rf " + Constants.USR_LOCAL_HOME
				+ "dhorse-agent-*.jar " + K8sUtils.AGENT_VOLUME_PATH + "dhorse-agent.jar"));
		
		V1VolumeMount volumeMount = new V1VolumeMount();
		volumeMount.setMountPath(K8sUtils.AGENT_VOLUME_PATH);
		volumeMount.setName(K8sUtils.AGENT_VOLUME);
		initContainer.setVolumeMounts(Arrays.asList(volumeMount));
		containers.add(initContainer);
	}
	
	private void initContainerOfWar(DeploymentContext context, List<V1Container> containers) {
		if(!warFileType(context.getApp())) {
			return;
		}
		
		V1Container container = new V1Container();
		container.setName("war");
		container.setImage(context.getFullNameOfImage());
		container.setImagePullPolicy("Always");
		container.setCommand(Arrays.asList("/bin/sh", "-c"));
		String warFile = Constants.USR_LOCAL_HOME + context.getApp().getAppName() + "." + PackageFileTypeEnum.WAR.getValue();
		container.setArgs(Arrays.asList("cp -rf " + warFile + " " + K8sUtils.TOMCAT_APP_PATH));
		
		V1VolumeMount volumeMount = new V1VolumeMount();
		volumeMount.setMountPath(K8sUtils.TOMCAT_APP_PATH);
		volumeMount.setName(K8sUtils.WAR_VOLUME);
		container.setVolumeMounts(Arrays.asList(volumeMount));
		containers.add(container);
	}
	
	private void initContainerOfNode(DeploymentContext context, List<V1Container> containers) {
		if(!TechTypeEnum.VUE.getCode().equals(context.getApp().getTechType())
				&& !TechTypeEnum.REACT.getCode().equals(context.getApp().getTechType())) {
			return;
		}
		
		V1Container container = new V1Container();
		container.setName("node");
		container.setImage(context.getFullNameOfImage());
		container.setImagePullPolicy("Always");
		container.setCommand(Arrays.asList("/bin/sh", "-c"));
		String file = Constants.USR_LOCAL_HOME + context.getApp().getAppName();
		container.setArgs(Arrays.asList("cp -rf " + file + "/* " + K8sUtils.NODE_VOLUME_PATH));
		
		V1VolumeMount volumeMount = new V1VolumeMount();
		volumeMount.setMountPath(K8sUtils.NODE_VOLUME_PATH);
		volumeMount.setName(K8sUtils.NODE_VOLUME);
		container.setVolumeMounts(Arrays.asList(volumeMount));
		containers.add(container);
	}
	
	private boolean warFileType(App app) {
		return springBootApp(app) && PackageFileTypeEnum.WAR.getCode()
					.equals(((AppExtendJava)app.getAppExtend()).getPackageFileType());
	}
	
	private boolean warFileType(AppPO appPO) {
		if(!springBootApp(appPO)) {
			return false;
		}
		AppExtendJava appExtend = JsonUtils.parseToObject(appPO.getExt(), AppExtendJava.class);
		return PackageFileTypeEnum.WAR.getCode().equals(appExtend.getPackageFileType());
	}
	
	private boolean jarFileType(App app) {
		return springBootApp(app) && PackageFileTypeEnum.JAR.getCode()
					.equals(((AppExtendJava)app.getAppExtend()).getPackageFileType());
	}
	
	private boolean jarFileType(AppPO appPO) {
		if(!springBootApp(appPO)) {
			return false;
		}
		AppExtendJava appExtend = JsonUtils.parseToObject(appPO.getExt(), AppExtendJava.class);
		return PackageFileTypeEnum.JAR.getCode().equals(appExtend.getPackageFileType());
	}
	
	private boolean springBootApp(AppPO appPO) {
		return TechTypeEnum.SPRING_BOOT.getCode().equals(appPO.getTechType());
	}

	private boolean springBootApp(App app) {
		return TechTypeEnum.SPRING_BOOT.getCode().equals(app.getTechType());
	}
	
	private boolean nodeApp(AppPO appPO) {
		return TechTypeEnum.VUE.getCode().equals(appPO.getTechType())
				|| TechTypeEnum.REACT.getCode().equals(appPO.getTechType());
	}

	private boolean nodeApp(App app) {
		return TechTypeEnum.VUE.getCode().equals(app.getTechType())
				|| TechTypeEnum.REACT.getCode().equals(app.getTechType());
	}
	
	private List<V1VolumeMount> volumeMounts(DeploymentContext context) {
		List<V1VolumeMount> volumeMounts = new ArrayList<>();
		
		//指定时区
		V1VolumeMount volumeMountTime = new V1VolumeMount();
		volumeMountTime.setName("timezone");
		volumeMountTime.setMountPath("/etc/localtime");
		volumeMounts.add(volumeMountTime);
		
		//data目录
		V1VolumeMount config = new V1VolumeMount();
		config.setName(K8sUtils.DATA_VOLUME);
		config.setMountPath(K8sUtils.DATA_PATH);
		volumeMounts.add(config);
		
		//临时data目录，用于下载文件等
		V1VolumeMount volumeMountData = new V1VolumeMount();
		volumeMountData.setMountPath(K8sUtils.TMP_DATA_PATH);
		volumeMountData.setName(K8sUtils.TMP_DATA_VOLUME);
		volumeMounts.add(volumeMountData);
		
		V1VolumeMount dhorseAgent = new V1VolumeMount();
		dhorseAgent.setMountPath(K8sUtils.AGENT_VOLUME_PATH);
		dhorseAgent.setName(K8sUtils.AGENT_VOLUME);
		volumeMounts.add(dhorseAgent);
		
		//war
		if(warFileType(context.getApp())) {
			V1VolumeMount volumeMountWar = new V1VolumeMount();
			volumeMountWar.setMountPath(K8sUtils.TOMCAT_APP_PATH);
			volumeMountWar.setName(K8sUtils.WAR_VOLUME);
			volumeMounts.add(volumeMountWar);
		}
		
		//Node
		if(nodeApp(context.getApp())) {
			V1VolumeMount volumeMountWar = new V1VolumeMount();
			volumeMountWar.setMountPath(K8sUtils.NODE_VOLUME_PATH);
			volumeMountWar.setName(K8sUtils.NODE_VOLUME);
			volumeMounts.add(volumeMountWar);
		}
		
		return volumeMounts;
	}

	private List<V1Volume> volumes(DeploymentContext context) {
		List<V1Volume> volumes = new ArrayList<>();
		
		V1Volume volumeTime = new V1Volume();
		volumeTime.setName("timezone");
		V1HostPathVolumeSource hostPathVolumeSource = new V1HostPathVolumeSource();
		hostPathVolumeSource.setPath("/etc/localtime");
		volumeTime.setHostPath(hostPathVolumeSource);
		volumes.add(volumeTime);
		
		V1Volume config = new V1Volume();
		config.setName(K8sUtils.DATA_VOLUME);
		V1ConfigMapVolumeSource s = new V1ConfigMapVolumeSource();
		s.setName(K8sUtils.DHORSE_CONFIGMAP_NAME);
		config.setConfigMap(s);
		volumes.add(config);
		
		//临时data目录，用于下载文件等
		V1Volume volumeData = new V1Volume();
		volumeData.setName(K8sUtils.TMP_DATA_VOLUME);
		V1EmptyDirVolumeSource emptyDirData = new V1EmptyDirVolumeSource();
		volumeData.setEmptyDir(emptyDirData);
		volumes.add(volumeData);
		
		V1Volume dhorseAgent = new V1Volume();
		dhorseAgent.setName(K8sUtils.AGENT_VOLUME);
		V1EmptyDirVolumeSource emptyDir = new V1EmptyDirVolumeSource();
		dhorseAgent.setEmptyDir(emptyDir);
		volumes.add(dhorseAgent);
		
		//War
		if(warFileType(context.getApp())) {
			V1Volume volumeWar = new V1Volume();
			volumeWar.setName(K8sUtils.WAR_VOLUME);
			V1EmptyDirVolumeSource emptyDirWar = new V1EmptyDirVolumeSource();
			volumeWar.setEmptyDir(emptyDirWar);
			volumes.add(volumeWar);
		}
		
		//Node
		if(nodeApp(context.getApp())) {
			V1Volume volumeWar = new V1Volume();
			volumeWar.setName(K8sUtils.NODE_VOLUME);
			V1EmptyDirVolumeSource emptyDirWar = new V1EmptyDirVolumeSource();
			volumeWar.setEmptyDir(emptyDirWar);
			volumes.add(volumeWar);
		}
		
		return volumes;
	}

	private ApiClient apiClient(String basePath, String accessToken) {
		return apiClient(basePath, accessToken, 1000, 1000);
	}

	private ApiClient apiClient(String basePath, String accessToken, int connectTimeout, int readTimeout) {
		ApiClient apiClient = new ClientBuilder()
				.setBasePath(basePath)
				.setVerifyingSsl(false)
				.setAuthentication(new AccessTokenAuthentication(accessToken))
				.build();
		apiClient.setConnectTimeout(connectTimeout);
		apiClient.setReadTimeout(readTimeout);
		return apiClient;
	}
	
	private boolean checkHealthOfAll(CoreV1Api coreApi, String namespace, String labelSelector) {
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
			V1PodList podList = null;
			try {
				podList = coreApi.listNamespacedPod(namespace, null, null, null, null, labelSelector, null,
						null, null, null, null);
			}catch (ApiException e) {
				//不打印中断异常的日志
				if(!e.getMessage().contains("InterruptedIOException")) {
					logger.error("Failed to list namespaced pod", e);
				}
				continue;
			}
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
	
	private boolean doCheckHealth(List<V1Pod> pods) {
		int runningService = 0;
		for (V1Pod pod : pods) {
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
		String labelSelector = K8sUtils.getDeploymentLabelSelector(appPO.getAppName(), appEnvPO.getTag());
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		V1PodList podList = null;
		try {
			podList = coreApi.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null,
					null, null);
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.REPLICA_LIST_FAILURE);
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
		List<V1Pod> pagePod = podList.getItems().subList(startOffset, endOffset);
		List<EnvReplica> pods = pagePod.stream().map(e -> {
			String imageName = imageName(appPO, e.getSpec());
			EnvReplica r = new EnvReplica();
			r.setVersionName(imageName.substring(imageName.lastIndexOf("/") + 1));
			r.setIp(e.getStatus().getPodIP());
			r.setName(e.getMetadata().getName());
			r.setEnvName(appEnvPO.getEnvName());
			r.setClusterName(clusterPO.getClusterName());
			r.setNamespace(namespace);
			//todo 这里为了解决k8s的时区问题，强制加8小时
			r.setStartTime(e.getMetadata().getCreationTimestamp().atZoneSameInstant(ZoneOffset.of("+08:00"))
					.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)));
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
	
	private String imageName(AppPO appPO, V1PodSpec podSpec) {
		if(jarFileType(appPO)) {
			return podSpec.getContainers().get(0).getImage();
		}else if(warFileType(appPO)) {
			for(V1Container initC : podSpec.getInitContainers()){
				if("war".equals(initC.getName())) {
					return initC.getImage();
				}
			}
		}else if(nodeApp(appPO)) {
			for(V1Container initC : podSpec.getInitContainers()){
				if("node".equals(initC.getName())) {
					return initC.getImage();
				}
			}
		}
		return null;
	}
	
	private Integer podStatus(V1PodStatus podStatus) {
		if("Pending".equals(podStatus.getPhase())) {
			return ReplicaStatusEnum.PENDING.getCode();
		}
		for(V1ContainerStatus containerStatus : podStatus.getContainerStatuses()) {
			V1ContainerStateTerminated terminated = containerStatus.getState().getTerminated();
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
		for(V1PodCondition c : podStatus.getConditions()) {
			if(!"True".equals(c.getStatus())) {
				return ReplicaStatusEnum.FAILED.getCode();
			}
		}
		return ReplicaStatusEnum.RUNNING.getCode();
	}

	public PodMetricsList replicaMetrics(ClusterPO clusterPO, String namespace) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		Metrics metrics = new Metrics(apiClient);
		try {
			return metrics.getPodMetrics(namespace);
		} catch (ApiException e) {
			logger.error("Failed to list pod metrics", e);
		}
		return null;
	}
	
	@Override
	public boolean rebuildReplica(ClusterPO clusterPO, String replicaName, String namespace) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		try {
			coreApi.deleteNamespacedPod(replicaName, namespace, null, null, null, null, null, null);
		} catch (ApiException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.REPLICA_RESTARTED_FAILURE);
		}
		return true;
	}

	public InputStream streamPodLog(ClusterPO clusterPO, String replicaName, String namespace) {
		PodLogs logs = new PodLogs(this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken(), 1 * 1000, 5 * 60 * 1000));
		try {
			return logs.streamNamespacedPodLog(namespace, replicaName, null, null, 2000, false);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.REPLICA_LOG_FAILED);
		}

		return null;
	}
	
	public String podLog(ClusterPO clusterPO, String replicaName, String namespace) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		try {
			//目前只支持下载最近50万行的日志
			return coreApi.readNamespacedPodLog(replicaName, namespace, null, null, null, null, null, null, null, 500000, null);
		} catch (ApiException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.REPLICA_RESTARTED_FAILURE);
		}
		return null;
	}

	public void openLogCollector(ClusterPO clusterPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		AppsV1Api appsApi = new AppsV1Api(apiClient);
		File fileBeatFile = new File(Constants.CONF_PATH + "filebeat-k8s.yml");
		if (!fileBeatFile.exists()) {
			LogUtils.throwException(logger, MessageCodeEnum.FILE_BEAT_K8S_FILE_INEXISTENCE);
		}
		try {
			List<Object> yamlObjects = Yaml.loadAll(fileBeatFile);
			for (Object o : yamlObjects) {
				if (o instanceof V1ConfigMap) {
					V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(K8sUtils.DHORSE_NAMESPACE, null,
							null, null, null, K8sUtils.getDeploymentLabelSelector("filebeat"), 1, null, null, null, null);
					if (CollectionUtils.isEmpty(configMapList.getItems())) {
						coreApi.createNamespacedConfigMap(K8sUtils.DHORSE_NAMESPACE, (V1ConfigMap) o, null, null, null,
								null);
					} else {
						coreApi.replaceNamespacedConfigMap("filebeat-config", K8sUtils.DHORSE_NAMESPACE,
								(V1ConfigMap) o, null, null, null, null);
					}
				} else if (o instanceof V1DaemonSet) {
					V1DaemonSetList daemonSetList = appsApi.listNamespacedDaemonSet(K8sUtils.DHORSE_NAMESPACE, null,
							null, null, null, K8sUtils.getDeploymentLabelSelector("filebeat"), 1, null, null, null, null);
					if (CollectionUtils.isEmpty(daemonSetList.getItems())) {
						appsApi.createNamespacedDaemonSet(K8sUtils.DHORSE_NAMESPACE, (V1DaemonSet) o, null, null, null,
								null);
					} else {
						appsApi.replaceNamespacedDaemonSet("filebeat", K8sUtils.DHORSE_NAMESPACE, (V1DaemonSet) o,
								null, null, null, null);
					}
				}
			}
		} catch (ApiException e) {
			LogUtils.throwException(logger, e.getResponseBody(), MessageCodeEnum.FAILURE);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
		}
	}

	public void closeLogCollector(ClusterPO clusterPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		AppsV1Api appsApi = new AppsV1Api(apiClient);
		try {
			V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(K8sUtils.DHORSE_NAMESPACE, null, null,
					null, null, K8sUtils.getDeploymentLabelSelector("filebeat"), 1, null, null, null, null);
			if (!CollectionUtils.isEmpty(configMapList.getItems())) {
				coreApi.deleteNamespacedConfigMap("filebeat-config", K8sUtils.DHORSE_NAMESPACE, null, null, null,
						null, null, null);
			}
			V1DaemonSetList daemonSetList = appsApi.listNamespacedDaemonSet(K8sUtils.DHORSE_NAMESPACE, null, null,
					null, null, K8sUtils.getDeploymentLabelSelector("filebeat"), 1, null, null, null, null);
			if (!CollectionUtils.isEmpty(daemonSetList.getItems())) {
				appsApi.deleteNamespacedDaemonSet("filebeat", K8sUtils.DHORSE_NAMESPACE, null, null, null, null,
						null, null);
			}
		} catch (ApiException e) {
			clusterError(e);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
	}
	
	private void clusterError(ApiException e) {
		String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
		if(message.contains("SocketTimeoutException")) {
			LogUtils.throwException(logger, message, MessageCodeEnum.CONNECT_CLUSTER_FAILURE);
		}
		LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
	}

	public boolean logSwitchStatus(ClusterPO clusterPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		AppsV1Api appsApi = new AppsV1Api(apiClient);
		try {
			V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(K8sUtils.DHORSE_NAMESPACE, null, null,
					null, null, K8sUtils.getDeploymentLabelSelector("filebeat"), 1, null, null, null, null);
			V1DaemonSetList daemonSetList = appsApi.listNamespacedDaemonSet(K8sUtils.DHORSE_NAMESPACE, null, null,
					null, null, K8sUtils.getDeploymentLabelSelector("filebeat"), 1, null, null, null, null);
			return !CollectionUtils.isEmpty(configMapList.getItems())
					&& !CollectionUtils.isEmpty(daemonSetList.getItems());
		} catch (ApiException e) {
			clusterError(e);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
		return false;
	}
	
	public List<String> queryFiles(ClusterPO clusterPO, String replicaName, String namespace) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		Exec exec = new Exec(apiClient);
		try {
			String[] commands = new String[] {"ls", K8sUtils.TMP_DATA_PATH};
			Process proc = exec.exec(namespace, replicaName, commands, true, true);
			final StringBuilder message = new StringBuilder();
			Thread out = new Thread(new Runnable() {
				public void run() {
					message.append(copy(proc.getInputStream()));
				}
			});
			out.start();
			proc.waitFor();
			out.join();
			proc.destroy();
			if (proc.exitValue() == 0) {
				return Arrays.asList(message.toString().replaceAll("\u001B\\[[\\d;]*[^\\d;]", "").split("\\s+"));
			}
			return null;
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
		} catch (IOException | InterruptedException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	private String copy(InputStream from) {
		BufferedReader in = null;
		Reader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new InputStreamReader(from);
			in = new BufferedReader(reader);
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				break;
			}
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
		} finally {
			try {
				if (from != null) {
					from.close();
				}
				if (reader != null) {
					reader.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
			}
		}

		return sb.toString();
	}
	
	public InputStream downloadFile(ClusterPO clusterPO, String namespace,  String replicaName, String fileName) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		Copy copy = new Copy(apiClient);
		try {
			return copy.copyFileFromPod(namespace, replicaName, K8sUtils.TMP_DATA_PATH + fileName);
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
		} catch (IOException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
		}
		return null;
	}
	
	public String getClusterVersion(String clusterUrl, String authToken) {
		ApiClient apiClient = this.apiClient(clusterUrl, authToken);
		VersionApi versionApi = new VersionApi(apiClient);
		try {
			return versionApi.getCode().getGitVersion();
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
		}
		return null;
	}
	
	public List<ClusterNamespace> namespaceList(ClusterPO clusterPO,
			ClusterNamespacePageParam pageParam) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		List<ClusterNamespace> namespaces = new ArrayList<>();
		String labelSelector = null;
		if(pageParam != null && !StringUtils.isBlank(pageParam.getNamespaceName())) {
			labelSelector = "kubernetes.io/metadata.name=" + pageParam.getNamespaceName();
		}
		try {
			V1NamespaceList namespaceList = coreApi.listNamespace(null, null, null, null, labelSelector, null, null, null, null, null);
			if(CollectionUtils.isEmpty(namespaceList.getItems())) {
				return namespaces;
			}
			for(V1Namespace n : namespaceList.getItems()) {
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
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_NAMESPACE_FAILURE);
		}
		return null;
	}
	
	public boolean addNamespace(ClusterPO clusterPO, String namespaceName) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		String labelSelector = "custom.name=" + namespaceName;
		try {
			V1NamespaceList namespaceList = coreApi.listNamespace(null, null, null, null, labelSelector, null, null, null, null, null);
			if(!CollectionUtils.isEmpty(namespaceList.getItems())) {
				logger.info("The namespace {} already exists ", namespaceName);
				return true;
			}
			V1ObjectMeta metaData = new V1ObjectMeta();
			metaData.setName(namespaceName);
			metaData.setLabels(Collections.singletonMap("custom.name", namespaceName));
			V1Namespace namespace = new V1Namespace();
			namespace.setMetadata(metaData);
			coreApi.createNamespace(namespace, null, null, null, null);
		} catch (ApiException e) {
			clusterError(e);
		}catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
		return true;
	}
	
	public boolean deleteNamespace(ClusterPO clusterPO, String namespaceName) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		try {
			coreApi.readNamespace(namespaceName, null);
			coreApi.deleteNamespace(namespaceName, null, null, null, null, null, null);
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DELETE_NAMESPACE_FAILURE);
		}catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
		return true;
	}
	
	/**
	 * 通过ConfigMap向k8s集群写入dhorse服务器的地址，地址格式为：ip1:8100,ip2:8100
	 */
	@Override
	public Void createDHorseConfig(ClusterPO clusterPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		V1ConfigMap configMap = new V1ConfigMap();
		configMap.setApiVersion("v1");
		configMap.setKind("ConfigMap");
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(K8sUtils.DHORSE_CONFIGMAP_NAME);
		meta.setLabels(Collections.singletonMap("app", K8sUtils.DHORSE_CONFIGMAP_NAME));
		configMap.setMetadata(meta);
		
		try {
			V1NamespaceList namespaceList = coreApi.listNamespace(null, null, null, null, null, null, null, null, null, null);
			if(CollectionUtils.isEmpty(namespaceList.getItems())) {
				return null;
			}
			ComponentConstants componentConstants = SpringBeanContext.getBean(ComponentConstants.class);
			for(V1Namespace n : namespaceList.getItems()) {
				String namespace = n.getMetadata().getName();
				if(!K8sUtils.DHORSE_NAMESPACE.equals(namespace)
						&& K8sUtils.getSystemNamspaces().contains(namespace)) {
					continue;
				}
				if(!"Active".equals(n.getStatus().getPhase())){
					continue;
				}
				V1ConfigMapList list = coreApi.listNamespacedConfigMap(namespace, null, null, null, null,
						"app=" + K8sUtils.DHORSE_CONFIGMAP_NAME, null, null, null, null, null);
				if(CollectionUtils.isEmpty(list.getItems())) {
					String ipPortUri = Constants.hostIp() + ";" + componentConstants.getServerPort() + ";" + Constants.COLLECT_METRICS_URI;
					configMap.setData(Collections.singletonMap(K8sUtils.DHORSE_SERVER_URL_KEY, ipPortUri));
					coreApi.createNamespacedConfigMap(namespace, configMap, null, null, null, null);
				}else {
					Set<String> newIp = new HashSet<>();
					newIp.add(Constants.hostIp() + ":" + componentConstants.getServerPort());
					
					configMap = list.getItems().get(0);
					String ipStr = configMap.getData().get(K8sUtils.DHORSE_SERVER_URL_KEY);
					if(!StringUtils.isBlank(ipStr)) {
						String[] ips = ipStr.split(",");
						for(String ip : ips) {
							if(!HttpUtils.pingDHorseServer(ip)) {
								continue;
							}
							newIp.add(ip);
						}
					}
					configMap.setData(Collections.singletonMap(K8sUtils.DHORSE_SERVER_URL_KEY, String.join(",", newIp)));
					coreApi.replaceNamespacedConfigMap(K8sUtils.DHORSE_CONFIGMAP_NAME,
							namespace, configMap, null, null, null, null);
				}
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DHORSE_SERVER_URL_FAILURE);
		}
		return null;
	}
	
	/**
	 * 通过ConfigMap向k8s集群删除dhorse服务器的地址，地址格式为：ip1:8100,ip2:8100
	 */
	@Override
	public Void deleteDHorseConfig(ClusterPO clusterPO) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		V1ConfigMap configMap = new V1ConfigMap();
		configMap.setApiVersion("v1");
		configMap.setKind("ConfigMap");
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(K8sUtils.DHORSE_CONFIGMAP_NAME);
		meta.setLabels(Collections.singletonMap("app", K8sUtils.DHORSE_CONFIGMAP_NAME));
		configMap.setMetadata(meta);
		
		try {
			V1NamespaceList namespaceList = coreApi.listNamespace(null, null, null, null, null, null, null, null, null, null);
			if(CollectionUtils.isEmpty(namespaceList.getItems())) {
				return null;
			}
			for(V1Namespace n : namespaceList.getItems()) {
				String namespace = n.getMetadata().getName();
				if(!K8sUtils.DHORSE_NAMESPACE.equals(namespace)
						&& K8sUtils.getSystemNamspaces().contains(namespace)) {
					continue;
				}
				if(!"Active".equals(n.getStatus().getPhase())){
					continue;
				}
				V1ConfigMapList list = coreApi.listNamespacedConfigMap(namespace, null, null, null, null,
						"app=" + K8sUtils.DHORSE_CONFIGMAP_NAME, null, null, null, null, null);
				if(CollectionUtils.isEmpty(list.getItems())) {
					continue;
				}
				
				Set<String> newIp = new HashSet<>();
				configMap = list.getItems().get(0);
				String ipStr = configMap.getData().get(K8sUtils.DHORSE_SERVER_URL_KEY);
				if(!StringUtils.isBlank(ipStr)) {
					String[] ips = ipStr.split(",");
					//ip格式为：127.0.0.1:8100
					for(String ip : ips) {
						if(Constants.hostIp().equals(ip.split(":")[0])) {
							continue;
						}
						if(!HttpUtils.pingDHorseServer(ip)) {
							continue;
						}
						newIp.add(ip);
					}
				}
				configMap.setData(Collections.singletonMap(K8sUtils.DHORSE_SERVER_URL_KEY, String.join(",", newIp)));
				coreApi.replaceNamespacedConfigMap(K8sUtils.DHORSE_CONFIGMAP_NAME,
						namespace, configMap, null, null, null, null);
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DHORSE_SERVER_URL_FAILURE);
		}
		return null;
	}
}