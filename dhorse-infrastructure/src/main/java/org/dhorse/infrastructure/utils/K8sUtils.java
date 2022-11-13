package org.dhorse.infrastructure.utils;

import java.util.HashSet;
import java.util.Set;

public class K8sUtils {
	
	public static final String DHORSE_NAMESPACE = "dhorse-system";
	
	public static final String DATA_PATH = "/tmp/data/";
	
	private static final Set<String> SYSTEM_NAMESPACES = new HashSet<>();
	
	static {
		SYSTEM_NAMESPACES.add("kube-node-lease");
		SYSTEM_NAMESPACES.add("kube-public");
		SYSTEM_NAMESPACES.add("kube-system");
		SYSTEM_NAMESPACES.add("dhorse-system");
	}
	
	public static String getReplicaAppName(String projectName, String projectEnvTag) {
		return new StringBuilder()
				.append(projectName).append("-")
				.append("1").append("-")
				.append(projectEnvTag).append("-")
				.append("dhorse")
				.toString();
	}
	
	public static String[] projectNameAndEnvTag(String podName) {
		String nameAndeEnv = podName.split("-dhorse-")[0];
		int offset = nameAndeEnv.lastIndexOf("-1-");
		String projectName = nameAndeEnv.substring(0, offset);
		String envTag = nameAndeEnv.substring(offset + 3);
		return new String[]{projectName, envTag};
	}
	
	public static String getDeploymentLabelSelector(String appName) {
		return "app=" + appName;
	}
	
	public static String getDeploymentName(String projectName, String projectEnvTag) {
		return getReplicaAppName(projectName, projectEnvTag);
	}
	
	public static String getDeploymentLabelSelector(String projectName, String projectEnvTag) {
		return "app=" + getReplicaAppName(projectName, projectEnvTag);
	}
	
	public static Set<String> getSystemNamspaces() {
		return SYSTEM_NAMESPACES;
	}
}