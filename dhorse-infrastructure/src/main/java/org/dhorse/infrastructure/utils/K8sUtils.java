package org.dhorse.infrastructure.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class K8sUtils {
	
	public static final String DHORSE_NAMESPACE = "dhorse-system";
	
	public static final String KUBE_SYSTEM_NAMESPACE = "kube-system";
	
	public static final String DOCKER_REGISTRY_KEY = "dhorse-docker-registry";
	
	public static final String AGENT_VOLUME_PATH = "/usr/local/agent/";
	
	public static final String DATA_VOLUME = "data-volume";
	
	public static final String DATA_PATH = "/usr/local/data/";
	
	public static final String NGINX_VOLUME_PATH = "/usr/share/nginx/html/";
	
	public static final String TOMCAT_APP_PATH = "/usr/local/tomcat/webapps/";
	
	public static final String AGENT_VOLUME = "agent-volume";
	
	public static final String WAR_VOLUME = "war-volume";
	
	public static final String NGINX_VOLUME = "nginx-volume";
	
	public static final String TMP_DATA_VOLUME = "tmp-data-volume";
	
	//用于存放临时数据，如下载文件等
	public static final String TMP_DATA_PATH = "/tmp/data/";
	
	public static final String APP_KEY = "app";
	
	public static final String DEFAULT_TOPOLOGY_KEY = "kubernetes.io/hostname";
	
	public static final String HEALTH_PATH = "/health";
	
	public static final String DHORSE_CONFIGMAP_NAME = "dhorse-config";
	
	public static final String DHORSE_SERVER_URL_KEY = "dhorse-server-url";
	
	public static final String DHORSE_LABEL_KEY = Constants.DHORSE_TAG + "-app";
	
	public static final String DHORSE_SELECTOR_KEY = DHORSE_LABEL_KEY + "=";
	
	private static final Set<String> SYSTEM_NAMESPACES = new HashSet<>();
	
	static {
		SYSTEM_NAMESPACES.add("kube-node-lease");
		SYSTEM_NAMESPACES.add("kube-public");
		SYSTEM_NAMESPACES.add("kube-system");
		SYSTEM_NAMESPACES.add("kube-flannel");
		SYSTEM_NAMESPACES.add(DHORSE_NAMESPACE);
	}
	
	public static String getDeploymentName(String appName, String appEnvTag) {
		return getReplicaAppName(appName, appEnvTag);
	}
	
	public static String getReplicaAppName(String appName, String appEnvTag) {
		return new StringBuilder()
				.append(appName).append("-")
				.append("1").append("-")
				.append(appEnvTag).append("-")
				.append("dhorse")
				.toString();
	}
	
	public static String getServiceName(String appName, String appEnvTag) {
		return new StringBuilder()
				.append(appName).append("-")
				.append(appEnvTag)
				.toString();
	}
	
	public static String[] appNameAndEnvTag(String podName) {
		String nameAndeEnv = podName.split("-dhorse-")[0];
		int offset = nameAndeEnv.lastIndexOf("-1-");
		String appName = nameAndeEnv.substring(0, offset);
		String envTag = nameAndeEnv.substring(offset + 3);
		return new String[]{appName, envTag};
	}
	
	public static String getDeploymentLabelSelector(String appName) {
		return DHORSE_SELECTOR_KEY + appName;
	}
	
	public static String getDeploymentLabelSelector(String appName, String appEnvTag) {
		return DHORSE_SELECTOR_KEY + getReplicaAppName(appName, appEnvTag);
	}
	
	public static Set<String> getSystemNamspaces() {
		return SYSTEM_NAMESPACES;
	}
	
	public static Map<String, String> dhorseLabel(String value) {
		Map<String, String> labels = new HashMap<>();
		labels.put(K8sUtils.DHORSE_LABEL_KEY, value);
		return labels;
	}
}