package org.dhorse.infrastructure.strategy.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.PackageFileTypeTypeEnum;
import org.dhorse.api.enums.ReplicaStatusEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespacePageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.AppEnv;
import org.dhorse.api.vo.AppExtendJava;
import org.dhorse.api.vo.ClusterNamespace;
import org.dhorse.api.vo.EnvReplica;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.model.Replica;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.DeployContext;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.kubernetes.client.Copy;
import io.kubernetes.client.Exec;
import io.kubernetes.client.KubernetesConstants;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.AutoscalingV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.VersionApi;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
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
import io.kubernetes.client.openapi.models.V1EmptyDirVolumeSource;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscaler;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscalerList;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscalerSpec;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1LifecycleHandler;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.openapi.models.V1TCPSocketAction;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;

public class K8sClusterStrategy implements ClusterStrategy {

	private static final Logger logger = LoggerFactory.getLogger(K8sClusterStrategy.class);

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
			Replica replica = new Replica();
			replica.setImageName(deployment.getItems().get(0).getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
			return replica;
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_DEPLOYMENT_FAILURE);
		}
		return null;
	}
	
	public boolean createDeployment(DeployContext context) {
		logger.info("Start to deploy k8s server");
		V1Deployment deployment = new V1Deployment();
		deployment.apiVersion("apps/v1");
		deployment.setKind("Deployment");
		deployment.setMetadata(deploymentMetaData(context.getDeploymentAppName()));
		deployment.setSpec(deploymentSpec(context));
		ApiClient apiClient = this.apiClient(context.getCluster().getClusterUrl(),
				context.getCluster().getAuthToken());
		AppsV1Api api = new AppsV1Api(apiClient);
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		String namespace = context.getAppEnv().getNamespaceName();
		String labelSelector = K8sUtils.getDeploymentLabelSelector(context.getDeploymentAppName());
		try {
			V1DeploymentList oldDeployment = api.listNamespacedDeployment(namespace, null, null, null, null,
					labelSelector, null, null, null, null, null);
			if (CollectionUtils.isEmpty(oldDeployment.getItems())) {
				deployment = api.createNamespacedDeployment(namespace, deployment, null, null, null, null);
			} else {
				deployment = api.replaceNamespacedDeployment(context.getDeploymentAppName(), namespace, deployment, null, null,
						null, null);
			}
			
			// 自动扩容任务
			createAutoScaling(context.getAppEnv(), context.getDeploymentAppName(), apiClient);

			//部署service
			createService(context);
			
			// 检查pod状态
			for (int i = 0; i < 60; i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ingore
				}
				//logger.info("Check Replicas, {} times", i + 1);
				V1PodList podList = coreApi.listNamespacedPod(namespace, null, null, null, null, labelSelector, null,
						null, null, null, null);
				if (CollectionUtils.isEmpty(podList.getItems())) {
					logger.warn("Replica size is 0");
					continue;
				}
				if (checkHealthOfAll(context.getAppEnv(), podList.getItems())) {
					logger.info("All replicas successfullly");
					return true;
				}
			}

			// 检查一定的次数之后，返回错误
			LogUtils.throwException(logger, MessageCodeEnum.CHECK_REPLICA_TIMEOUT);
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
	
	private boolean createService(DeployContext context) throws ApiException {
		logger.info("Start to create service");
		ApiClient apiClient = this.apiClient(context.getCluster().getClusterUrl(),
				context.getCluster().getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		String namespace = context.getAppEnv().getNamespaceName();
		String appName = context.getApp().getAppName();
		V1ServiceList serviceList = coreApi.listNamespacedService(namespace, null, null, null, null,
				"app=" + appName, 1, null, null, null, null);
		V1Service service = null;
		if (CollectionUtils.isEmpty(serviceList.getItems())) {
			service = new V1Service();
			service.apiVersion("v1");
			service.setKind("Service");
			service.setMetadata(serviceMeta(appName));
			service.setSpec(serviceSpec(context));
			service = coreApi.createNamespacedService(namespace, service, null, null, null, null);
		} else {
			service = serviceList.getItems().get(0);
			modifyServiceSpec(context, service);
			service = coreApi.replaceNamespacedService(context.getApp().getAppName(), namespace, service, null, null, null, null);
		}
		logger.info("End to create service");
		return true;
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
			if (CollectionUtils.isEmpty(oldDeployment.getItems())) {
				return true;
			}
			V1Status status = api.deleteNamespacedDeployment(depolymentName, namespace, null, null, null, null, null, null);
			if(status == null || !KubernetesConstants.V1STATUS_SUCCESS.equals(status.getStatus())){
				return false;
			}
			if(!deleteAutoScaling(namespace, depolymentName, apiClient)) {
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
		String appName = K8sUtils.getReplicaAppName(appPO.getAppName(), appEnvPO.getTag());
		return createAutoScaling(appEnvPO, appName, apiClient);
	}

	private boolean createAutoScaling(AppEnvPO appEnvPO, String appName, ApiClient apiClient) {
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
		scaleTargetRef.setName(appName);
		spec.setScaleTargetRef(scaleTargetRef);
		spec.setTargetCPUUtilizationPercentage(appEnvPO.getAutoScalingCpu());
		body.setMetadata(deploymentMetaData(appName));
		body.setSpec(spec);
		String labelSelector = K8sUtils.getDeploymentLabelSelector(appName);
		try {
			V1HorizontalPodAutoscalerList autoscalerList = autoscalingApi.listNamespacedHorizontalPodAutoscaler(
					appEnvPO.getNamespaceName(), null, null, null, null, labelSelector, 1, null, null, null,
					null);
			if (CollectionUtils.isEmpty(autoscalerList.getItems())) {
				autoscalingApi.createNamespacedHorizontalPodAutoscaler(appEnvPO.getNamespaceName(), body, null,
						null, null, null);
			} else {
				autoscalingApi.replaceNamespacedHorizontalPodAutoscaler(appName, appEnvPO.getNamespaceName(),
						body, null, null, null, null);
			}
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.REPLICA_RESTARTED_FAILURE);
		}
		return true;
	}
	
	private boolean deleteAutoScaling(String namespace, String appName, ApiClient apiClient) {
		AutoscalingV1Api autoscalingApi = new AutoscalingV1Api(apiClient);
		String labelSelector = K8sUtils.getDeploymentLabelSelector(appName);
		try {
			V1HorizontalPodAutoscalerList autoscalerList = autoscalingApi.listNamespacedHorizontalPodAutoscaler(
					namespace, null, null, null, null, labelSelector, 1, null, null, null, null);
			if (CollectionUtils.isEmpty(autoscalerList.getItems())) {
				return true;
			}
			V1Status status = autoscalingApi.deleteNamespacedHorizontalPodAutoscaler(appName,
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
	
	private void modifyServiceSpec(DeployContext context, V1Service service) {
		V1ServicePort port = service.getSpec().getPorts().get(0);
		port.setPort(context.getAppEnv().getServicePort());
		port.setTargetPort(new IntOrString(context.getAppEnv().getServicePort()));
	}
	
	private V1ServiceSpec serviceSpec(DeployContext context) {
		V1ServicePort port = new V1ServicePort();
		port.setProtocol("TCP");
		port.setPort(context.getAppEnv().getServicePort());
		port.setTargetPort(new IntOrString(context.getAppEnv().getServicePort()));
		
		V1ServiceSpec spec = new V1ServiceSpec();
		spec.setSelector(Collections.singletonMap("app", context.getDeploymentAppName()));
		spec.setPorts(Arrays.asList(port));
		return spec;
	}
	
	private V1ObjectMeta deploymentMetaData(String appName) {
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(appName);
		metadata.setLabels(Collections.singletonMap("app", appName));
		return metadata;
	}

	private V1DeploymentSpec deploymentSpec(DeployContext context) {
		V1DeploymentSpec spec = new V1DeploymentSpec();
		spec.setReplicas(context.getAppEnv().getMinReplicas());
		spec.setSelector(specSelector(context.getDeploymentAppName()));
		spec.setTemplate(specTemplate(context));
		return spec;
	}

	private V1LabelSelector specSelector(String appName) {
		V1LabelSelector selector = new V1LabelSelector();
		selector.setMatchLabels(Collections.singletonMap("app", appName));
		return selector;
	}
	
	private V1PodTemplateSpec specTemplate(DeployContext context) {
		Map<String, String> labels = new HashMap<>();
		labels.put("app", context.getDeploymentAppName());
		labels.put("version", String.valueOf(System.currentTimeMillis()));
		V1ObjectMeta specMetadata = new V1ObjectMeta();
		specMetadata.setLabels(labels);
		V1PodTemplateSpec template = new V1PodTemplateSpec();
		template.setMetadata(specMetadata);
		template.setSpec(podSpec(context));
		return template;
	}

	private V1PodSpec podSpec(DeployContext context) {
		V1PodSpec podSpec = new V1PodSpec();
		podSpec.setInitContainers(initContainer(context));
		podSpec.setContainers(containers(context));
		podSpec.setVolumes(volumes(context));
		return podSpec;
	}
	
	private List<V1Container> containers(DeployContext context) {
		V1Container container = new V1Container();
		container.setName(context.getDeploymentAppName());
		container.setImage(context.getFullNameOfImage());
		commands(container, context);
		args(container, context);
		container.setImagePullPolicy("Always");
		V1ContainerPort containerPort = new V1ContainerPort();
		containerPort.setContainerPort(context.getAppEnv().getServicePort());
		container.setPorts(Arrays.asList(containerPort));
		
		// 设置资源
		Map<String, Quantity> requests = new HashMap<>();
		requests.put("memory", new Quantity(context.getAppEnv().getReplicaMemory() + "Mi"));
		requests.put("cpu", new Quantity(context.getAppEnv().getReplicaCpu().toString()));
		Map<String, Quantity> limits = new HashMap<>();
		limits.put("memory", new Quantity(context.getAppEnv().getReplicaMemory() + "Mi"));
		limits.put("cpu", new Quantity(context.getAppEnv().getReplicaCpu().toString()));
		V1ResourceRequirements resources = new V1ResourceRequirements();
		resources.setRequests(requests);
		resources.setLimits(limits);
		container.setResources(resources);
		container.setVolumeMounts(volumeMounts(context));
		probe(container, context);
		lifecycle(container, context);
		
		return Arrays.asList(container);
	}
	
	private void lifecycle(V1Container container, DeployContext context) {
		V1ExecAction exec = new V1ExecAction();
		exec.command(Arrays.asList("sh", "-c", "sleep 2"));
		V1LifecycleHandler preStop = new V1LifecycleHandler();
		preStop.setExec(exec);
		V1Lifecycle lifecycle = new V1Lifecycle();
		lifecycle.setPreStop(preStop);
		container.setLifecycle(lifecycle);
	}

	private void probe(V1Container container, DeployContext context) {
		if(context.getAppEnv().getServicePort() == null) {
			return;
		}
		
		V1Probe startupProbe = new V1Probe();
		V1Probe readinessProbe = new V1Probe();
		V1Probe livenessProbe = new V1Probe();
		//如果没有设置健康检查路径，就用tcp检查，否则就用http检查
		if(StringUtils.isBlank(context.getAppEnv().getHealthPath())) {
			V1TCPSocketAction tcpAction = new V1TCPSocketAction();
			tcpAction.setPort(new IntOrString(context.getAppEnv().getServicePort()));
			startupProbe.setTcpSocket(tcpAction);
			readinessProbe.setTcpSocket(tcpAction);
			livenessProbe.setTcpSocket(tcpAction);
		}else {
			V1HTTPGetAction getAction = new V1HTTPGetAction();
			getAction.setPath(context.getAppEnv().getHealthPath());
			getAction.setPort(new IntOrString(context.getAppEnv().getServicePort()));
			startupProbe.setHttpGet(getAction);
			readinessProbe.setHttpGet(getAction);
			livenessProbe.setHttpGet(getAction);
		}
		//启动检查
		startupProbe.setInitialDelaySeconds(6);
		startupProbe.setPeriodSeconds(1);
		startupProbe.setTimeoutSeconds(1);
		startupProbe.setSuccessThreshold(1);
		startupProbe.setFailureThreshold(30);
		container.startupProbe(startupProbe);
		//就绪检查
		readinessProbe.setInitialDelaySeconds(6);
		readinessProbe.setPeriodSeconds(30);
		readinessProbe.setTimeoutSeconds(1);
		readinessProbe.setSuccessThreshold(1);
		readinessProbe.setFailureThreshold(3);
		container.setReadinessProbe(readinessProbe);
		//存活检查
		livenessProbe.setInitialDelaySeconds(30);
		livenessProbe.setPeriodSeconds(30);
		livenessProbe.setTimeoutSeconds(1);
		livenessProbe.setSuccessThreshold(1);
		livenessProbe.setFailureThreshold(3);
		container.livenessProbe(livenessProbe);
	}
	
	private void commands(V1Container container, DeployContext context){
		List<String> commands = new ArrayList<>();
		commands.add("java");
		//jvm参数
		if (!StringUtils.isBlank(context.getAppEnv().getJvmArgs())) {
			String[] jvmArgs = context.getAppEnv().getJvmArgs().split("\\s+");
			for (String arg : jvmArgs) {
				commands.add(arg);
			}
		}
		//skywalking-agent参数
		if(YesOrNoEnum.YES.getCode().equals(context.getAppEnv().getTraceStatus())) {
			TraceTemplate traceTemplate = context.getGlobalConfigAgg().getTraceTemplate(context.getAppEnv().getTraceTemplateId());
			if(traceTemplate == null) {
				LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_IS_EMPTY);
			}
			commands.add("-javaagent:/tmp/skywalking-agent/skywalking-agent.jar");
			commands.add("-Dskywalking.collector.backend_service=" + traceTemplate.getServiceUrl());
			commands.add("-Dskywalking.agent.service_name=" + context.getApp().getAppName());
		}
		commands.add("-Duser.timezone=Asia/Shanghai");
		commands.add("-Denv=" + context.getAppEnv().getTag());
		commands.add("-jar");
		String packageFileType = PackageFileTypeTypeEnum.getByCode(((AppExtendJava)context.getApp().getAppExtend()).getPackageFileType()).getValue();
		commands.add(context.getApp().getAppName() + "." + packageFileType);
		
		container.setCommand(commands);
	}
	
	private void args(V1Container container, DeployContext context){
		List<String> args = new ArrayList<>();
		args.add("--server.port=" + context.getAppEnv().getServicePort());
		container.setArgs(args);
	}
	
	private List<V1Container> initContainer(DeployContext context) {
		if(YesOrNoEnum.NO.getCode().equals(context.getAppEnv().getTraceStatus())) {
			return null;
		}
		
		TraceTemplate traceTemplate = context.getGlobalConfigAgg().getTraceTemplate(context.getAppEnv().getTraceTemplateId());
		if(traceTemplate == null) {
			LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_IS_EMPTY);
		}
		
		V1Container initContainer = new V1Container();
		initContainer.setName("skywalking-agent");
		initContainer.setImage(context.getFullNameOfAgentImage());
		initContainer.setImagePullPolicy("Always");
		List<String> command = Arrays.asList("/bin/sh", "-c");
		initContainer.setCommand(command);
		List<String> args = Arrays.asList("cp -rf /skywalking-agent /tmp");
		initContainer.setArgs(args);
		
		V1VolumeMount agentVolumeMount = new V1VolumeMount();
		agentVolumeMount.setMountPath("/tmp");
		agentVolumeMount.setName("skw-agent-volume");
		initContainer.setVolumeMounts(Arrays.asList(agentVolumeMount));
		return Arrays.asList(initContainer);
	}
	
	private List<V1VolumeMount> volumeMounts(DeployContext context) {
		List<V1VolumeMount> volumeMounts = new ArrayList<>();
		
		//指定时区
//		V1VolumeMount volumeMountTime = new V1VolumeMount();
//		volumeMountTime.setName("timezone");
//		volumeMountTime.setMountPath("/etc/localtime");
//		volumeMountTime.setReadOnly(true);
//		volumeMounts.add(volumeMountTime);
		
		//data目录
		V1VolumeMount volumeMountData = new V1VolumeMount();
		volumeMountData.setMountPath(K8sUtils.DATA_PATH);
		volumeMountData.setName("data-volume");
		volumeMounts.add(volumeMountData);
		
		//skyWalking-agent
		if(YesOrNoEnum.YES.getCode().equals(context.getAppEnv().getTraceStatus())) {
			V1VolumeMount volumeMountAgent = new V1VolumeMount();
			volumeMountAgent.setMountPath("/tmp");
			volumeMountAgent.setName("skw-agent-volume");
			volumeMounts.add(volumeMountAgent);
		}
		
		return volumeMounts;
	}

	private List<V1Volume> volumes(DeployContext context) {
		List<V1Volume> volumes = new ArrayList<>();
		
//		V1Volume volumeTime = new V1Volume();
//		volumeTime.setName("timezone");
//		V1HostPathVolumeSource hostPathVolumeSource = new V1HostPathVolumeSource();
//		hostPathVolumeSource.setPath("/usr/share/zoneinfo/Asia/Shanghai");
//		volumeTime.setHostPath(hostPathVolumeSource);
//		volumes.add(volumeTime);
		
		//data目录
		V1Volume volumeData = new V1Volume();
		volumeData.setName("data-volume");
		V1EmptyDirVolumeSource emptyDirData = new V1EmptyDirVolumeSource();
		volumeData.setEmptyDir(emptyDirData);
		volumes.add(volumeData);
		
		//skyWalking-agent
		if(YesOrNoEnum.YES.getCode().equals(context.getAppEnv().getTraceStatus())) {
			V1Volume volumeAgent = new V1Volume();
			volumeAgent.setName("skw-agent-volume");
			V1EmptyDirVolumeSource emptyDir = new V1EmptyDirVolumeSource();
			volumeAgent.setEmptyDir(emptyDir);
			volumes.add(volumeAgent);
		}
		
		return volumes;
	}

	private ApiClient apiClient(String basePath, String accessToken) {
		return apiClient(basePath, accessToken, 1000, 1000);
	}

	private ApiClient apiClient(String basePath, String accessToken, int connectTimeout, int readTimeout) {
		ApiClient apiClient = new ClientBuilder().setBasePath(basePath).setVerifyingSsl(false)
				.setAuthentication(new AccessTokenAuthentication(accessToken)).build();
		apiClient.setConnectTimeout(connectTimeout);
		apiClient.setReadTimeout(readTimeout);
		return apiClient;
	}
	
	private boolean checkHealthOfAll(AppEnvPO env, List<V1Pod> pods) {
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
			EnvReplica r = new EnvReplica();
			//目前只有一个container
			String imageName = e.getSpec().getContainers().get(0).getImage();
			r.setVersionName(imageName.substring(imageName.lastIndexOf("/") + 1));
			r.setIp(e.getStatus().getPodIP());
			r.setName(e.getMetadata().getName());
			r.setEnvName(appEnvPO.getEnvName());
			r.setClusterName(clusterPO.getClusterName());
			r.setNamespace(namespace);
			//todo 这里为了解决k8s的时区问题，强制加8小时
			r.setStartTime(e.getMetadata().getCreationTimestamp().atZoneSameInstant(ZoneOffset.of("+08:00"))
					.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)));
			//k8s 1.19版本以下的api
			//r.setStartTime(e.getMetadata().getCreationTimestamp().withZone(DateTimeZone.forOffsetHours(8))
			//		.toString(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS));
			r.setStatus(podStatus(e.getStatus()));
			return r;
		}).collect(Collectors.toList());
		pageData.setItems(pods);
		pageData.setPageNum(pageNum);
		pageData.setPageCount(pageCount);
		pageData.setPageSize(pageParam.getPageSize());
		pageData.setItemCount(dataCount);

		return pageData;
	}
	
	private Integer podStatus(V1PodStatus podStatus) {
		if("Pending".equals(podStatus.getPhase())) {
			return ReplicaStatusEnum.PENDING.getCode();
		}
		for(V1ContainerStatus containerStatus : podStatus.getContainerStatuses()) {
			V1ContainerStateTerminated terminated = containerStatus.getState().getTerminated();
			if(terminated != null) {
				return ReplicaStatusEnum.DESTROYING.getCode();
			}
			if(!containerStatus.getStarted().booleanValue() || !containerStatus.getReady().booleanValue()) {
				return ReplicaStatusEnum.PENDING.getCode();
			}
		}
		return ReplicaStatusEnum.RUNNING.getCode();
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
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
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
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
		return false;
	}
	
	public List<String> queryFiles(ClusterPO clusterPO, String replicaName, String namespace) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		Exec exec = new Exec(apiClient);
		try {
			String[] commands = new String[] {"ls", K8sUtils.DATA_PATH};
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
			return copy.copyFileFromPod(namespace, replicaName, K8sUtils.DATA_PATH + fileName);
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
			ClusterNamespacePageParam clusterNamespacePageParam) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		List<ClusterNamespace> namespaces = new ArrayList<>();
		String labelSelector = null;
		if(!StringUtils.isBlank(clusterNamespacePageParam.getNamespaceName())) {
			labelSelector = "kubernetes.io/metadata.name=" + clusterNamespacePageParam.getNamespaceName();
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
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.ADD_NAMESPACE_FAILURE);
		}
		return true;
	}
	
	public boolean deleteNamespace(ClusterPO clusterPO, String namespaceName) {
		ApiClient apiClient = this.apiClient(clusterPO.getClusterUrl(), clusterPO.getAuthToken());
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		String labelSelector = "custom.name=" + namespaceName;
		try {
			V1NamespaceList namespaceList = coreApi.listNamespace(null, null, null, null, labelSelector, null, null, null, null, null);
			if(CollectionUtils.isEmpty(namespaceList.getItems())) {
				LogUtils.throwException(logger, MessageCodeEnum.NAMESPACE_INEXISTENCE);
			}
			V1ObjectMeta metaData = new V1ObjectMeta();
			metaData.setName(namespaceName);
			V1Namespace namespace = new V1Namespace();
			namespace.setMetadata(metaData);
			coreApi.deleteNamespace(namespaceName, null, null, null, null, null, null);
		} catch (ApiException e) {
			String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
			LogUtils.throwException(logger, message, MessageCodeEnum.DELETE_NAMESPACE_FAILURE);
		}
		return true;
	}
}
