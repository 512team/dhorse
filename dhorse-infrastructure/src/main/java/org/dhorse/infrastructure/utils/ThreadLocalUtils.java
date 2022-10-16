package org.dhorse.infrastructure.utils;

public class ThreadLocalUtils {
	
	private static final InheritableThreadLocal<DeployContext> DEPLOY_CONTEXT = new InheritableThreadLocal<>();
	
	public static void putDeployContext(DeployContext id) {
		DEPLOY_CONTEXT.set(id);
	}
	
	public static DeployContext getDeployContext() {
		return DEPLOY_CONTEXT.get();
	}
	
	public static void removeDeployContext() {
		DEPLOY_CONTEXT.remove();
	}
}
