package org.dhorse.infrastructure.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 部署线程池。
 *
 * @author Dahai
 **/
public class DeploymentThreadPoolUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(DeploymentThreadPoolUtils.class);
	
	private static Map<String, Thread> THREAD_MAP = new ConcurrentHashMap<>();
	
	public static void put(String k, Thread t) {
		THREAD_MAP.put(k, t);
	}
	
	public static Thread get(String k) {
		return THREAD_MAP.get(k);
	}
	
	public static void remove(String k) {
		THREAD_MAP.remove(k);
	}
	
	public static void interrupt(String k) {
		Thread t = THREAD_MAP.get(k);
		if(t == null) {
			return;
		}
		t.interrupt();
		logger.info("Abort thread name:{}", t.getName());
		t.interrupt();
	}
	
	public static boolean interrupted() {
		//恢复中断状态
		if(Thread.interrupted()) {
			logger.warn("The deployment was manually aborted");
			return true;
		}
		return false;
	}
}
