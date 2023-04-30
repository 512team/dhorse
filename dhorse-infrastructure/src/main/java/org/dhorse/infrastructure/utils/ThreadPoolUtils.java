package org.dhorse.infrastructure.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 线程池。
 *
 * @author Dahai
 **/
public class ThreadPoolUtils {

	private static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();

	private static final ThreadPoolExecutor THREAD_POOL_WRITE_LOG = new ThreadPoolExecutor(CPU_SIZE, 2 * CPU_SIZE, 5,
			TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
			new ThreadFactoryBuilder().setNameFormat("write-log-pool-%d").build());

	private static final ThreadPoolExecutor THREAD_POOL_TERMINAL = new ThreadPoolExecutor(10, 100, 5, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(1), new ThreadFactoryBuilder().setNameFormat("terminal-pool-%d").build());

	private static final ExecutorService SINGLE_THREAD = Executors.newSingleThreadExecutor();

	private static final ThreadPoolExecutor THREAD_POOL_BUILD_VERSION = new ThreadPoolExecutor(10, 100, 5,
			TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1),
			new ThreadFactoryBuilder().setNameFormat("build-version-pool-%d").build());

	private static final ThreadPoolExecutor THREAD_POOL_DEPLOY = new ThreadPoolExecutor(10, 100, 5, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(1), new SimpleThreadFactory());

	public static void writeLog(Callable<Void> call) {
		THREAD_POOL_WRITE_LOG.submit(call);
	}

	public static void terminal(Runnable runnable) {
		THREAD_POOL_TERMINAL.submit(runnable);
	}

	public static void buildVersion(Runnable runnable) {
		THREAD_POOL_BUILD_VERSION.submit(runnable);
	}

	public static void deploy(Runnable runnable) {
		THREAD_POOL_DEPLOY.submit(runnable);
	}

	public static void async(Runnable runnable) {
		SINGLE_THREAD.submit(runnable);
	}

	private static class SimpleThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			return new Thread(r, "deploy-pool-" + System.currentTimeMillis() + (int)((Math.random()*9+1)*1000));
		}
	}
}
