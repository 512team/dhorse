package org.dhorse.infrastructure.utils;

import java.util.HashSet;
import java.util.Set;

public class K8sUtils {
	
	public static final String DHORSE_NAMESPACE = "dhorse-system";
	
	public static final String DATA_PATH = "/tmp/data/";
	
	public static final String APP_KEY = "app";
	
	public static final String DEFAULT_TOPOLOGY_KEY = "kubernetes.io/hostname";
	
	public static final String HEALTH_PATH = "/health";
	
	private static final Set<String> SYSTEM_NAMESPACES = new HashSet<>();
	
	static {
		SYSTEM_NAMESPACES.add("kube-node-lease");
		SYSTEM_NAMESPACES.add("kube-public");
		SYSTEM_NAMESPACES.add("kube-system");
		SYSTEM_NAMESPACES.add("dhorse-system");
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
		return "app=" + appName;
	}
	
	public static String getDeploymentLabelSelector(String appName, String appEnvTag) {
		return "app=" + getReplicaAppName(appName, appEnvTag);
	}
	
	public static String getDhorseLabelSelector(String envTag) {
		return Constants.DHORSE_TAG + "-" + envTag;
	}
	
	public static Set<String> getSystemNamspaces() {
		return SYSTEM_NAMESPACES;
	}
}