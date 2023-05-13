package org.dhorse.infrastructure.utils;

public class ThreadLocalUtils {
	
	/**
	 * 部署时的线程缓存
	 */
	public static class Deployment {
		
		private static final ThreadLocal<DeploymentContext> CACHE = new InheritableThreadLocal<>();
		
		public static void put(DeploymentContext val) {
			CACHE.set(val);
		}
		
		public static DeploymentContext get() {
			return CACHE.get();
		}
		
		public static void remove() {
			CACHE.remove();
		}
	}
	
	/**
	 * 动态表的线程缓存
	 */
	public static class DynamicTable {
		
		private static final ThreadLocal<String> CACHE = new InheritableThreadLocal<>();
		
		public static void put(String val) {
			CACHE.set(val);
		}
		
		public static String get() {
			return CACHE.get();
		}
		
		public static void remove() {
			CACHE.remove();
		}
	}
}
