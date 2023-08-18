package org.dhorse.infrastructure.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.component.SpringBeanContext;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 线程池管理
 *
 * @author Dahai
 **/
public class ThreadPoolUtils {

	private static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();

	private static final int CPU_SIZE_2 = 2 * CPU_SIZE;
	
	private static final int CPU_SIZE_4 = 4 * CPU_SIZE;
	
	private static ThreadPoolExecutor THREAD_POOL_BUILD;

	private static ThreadPoolExecutor THREAD_POOL_DEPLOYMENT;
	
	private static final ExecutorService SINGLE_THREAD = Executors.newSingleThreadExecutor();
	
	private static final ThreadPoolExecutor THREAD_POOL_WRITE_LOG = new ThreadPoolExecutor(
			CPU_SIZE,
			CPU_SIZE_2,
			5,
			TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(1000),
			threadFactory("write-log-pool-%d"));

	private static ThreadPoolExecutor THREAD_POOL_TERMINAL = new ThreadPoolExecutor(
			10,
			100,
			5,
			TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(1),
			threadFactory("terminal-pool-%d"));

	public static void initThreadPool() {
		ComponentConstants componentConstants = SpringBeanContext.getBean(ComponentConstants.class);
		int buildCore = CPU_SIZE_2;
		int buildMax = CPU_SIZE_4;
		int deploymentCore = CPU_SIZE_2;
		int deploymentMax = CPU_SIZE_4;
		if(componentConstants.getThreadPoolBuildCore() != null) {
			buildCore = componentConstants.getThreadPoolBuildCore();
		}
		if(componentConstants.getThreadPoolBuildMax() != null) {
			buildMax = componentConstants.getThreadPoolBuildMax();
		}
		if(componentConstants.getThreadPoolDeploymentCore() != null) {
			deploymentCore = componentConstants.getThreadPoolDeploymentCore();
		}
		if(componentConstants.getThreadPoolDeploymentMax() != null) {
			deploymentMax = componentConstants.getThreadPoolDeploymentMax();
		}
		
		THREAD_POOL_BUILD = new ThreadPoolExecutor(
				buildCore,
				buildMax,
				5,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(5),
				threadFactory("build-pool-%d"));

		THREAD_POOL_DEPLOYMENT = new ThreadPoolExecutor(
				deploymentCore,
				deploymentMax,
				5,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(5),
				threadFactory("deployment-pool-%d"));
	}

	public static void writeLog(Callable<Void> call) {
		THREAD_POOL_WRITE_LOG.submit(call);
	}

	public static void terminal(Runnable runnable) {
		THREAD_POOL_TERMINAL.submit(runnable);
	}

	public static void buildVersion(Runnable runnable) {
		THREAD_POOL_BUILD.submit(runnable);
	}

	public static void deploy(Runnable runnable) {
		THREAD_POOL_DEPLOYMENT.submit(runnable);
	}

	public static void async(Runnable runnable) {
		SINGLE_THREAD.submit(runnable);
	}
	
	private static ThreadFactory threadFactory(String nameFormat) {
		return new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
	}
}
