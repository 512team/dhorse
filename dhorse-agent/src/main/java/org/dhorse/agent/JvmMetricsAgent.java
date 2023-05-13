package org.dhorse.agent;

import java.lang.instrument.Instrumentation;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 采集Jvm信息的agent
 * <p>
 * 启动方式：java
 * -javaagent:dhorse-agent.jar=http://dhost_host:port/app/env/replica/metrics/add
 * 
 * @author 无双
 */
public class JvmMetricsAgent {

	private static final ScheduledExecutorService SCHEDULED = Executors.newSingleThreadScheduledExecutor();

	public static void premain(String args, Instrumentation inst) {
		
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println(String.format("Failed to get localhost name, message: %s", e));
		}
		final String replicaName = hostName;
		SCHEDULED.scheduleAtFixedRate(() -> {
			List<Metrics> metricsList = new ArrayList<>(16);
			memoryHeap(replicaName, metricsList);
			memoryPool(replicaName, metricsList);
			gc(replicaName, metricsList);
			thread(replicaName, metricsList);
			HttpUtils.sendPost(args, metricsList.toString());
		}, 0, 10, TimeUnit.SECONDS);
	}

	private static void memoryHeap(String replicaName, List<Metrics> metricsList) {
		MemoryUsage m = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		item(MetricsTypeEnum.HEAP_MEMORY_USED, replicaName, metricsList, m.getUsed());
		item(MetricsTypeEnum.HEAP_MEMORY_MAX, replicaName, metricsList, m.getMax());
	}

	private static void memoryPool(String replicaName, List<Metrics> metricsList) {
		List<MemoryPoolMXBean> mpxbs = ManagementFactory.getMemoryPoolMXBeans();
		long young = 0L;
		for (MemoryPoolMXBean mp : mpxbs) {
			String name = mp.getName().toLowerCase();
			if (name.contains("metaspace")) {
				item(MetricsTypeEnum.META_MEMORY_MAX, replicaName, metricsList, mp.getUsage().getMax());
				item(MetricsTypeEnum.META_MEMORY_USED, replicaName, metricsList, mp.getUsage().getUsed());
				continue;
			}
			if (name.contains("eden") || name.contains("SURVIVOR")) {
				young += mp.getUsage().getUsed();
				continue;
			}
		}
		
		//年轻代
		item(MetricsTypeEnum.YOUNG, replicaName, metricsList, young);
	}
	
	private static void gc(String replicaName, List<Metrics> metricsList) {
		List<GarbageCollectorMXBean> gcbs = ManagementFactory.getGarbageCollectorMXBeans();
		long size = 0L;
		long duration = 0L;
		for (GarbageCollectorMXBean gcb : gcbs) {
			size += gcb.getCollectionCount();
			duration += gcb.getCollectionTime();
		}
		
		item(MetricsTypeEnum.GC_SIZE, replicaName, metricsList, size);
		item(MetricsTypeEnum.GC_DURATION, replicaName, metricsList, duration);
	}

	private static void thread(String replicaName, List<Metrics> metricsList) {
		ThreadMXBean txb = ManagementFactory.getThreadMXBean();

		item(MetricsTypeEnum.THREAD, replicaName, metricsList, (long) txb.getThreadCount());
		item(MetricsTypeEnum.THREAD_DAEMON, replicaName, metricsList, (long) txb.getDaemonThreadCount());

		ThreadInfo[] threads = txb.getThreadInfo(txb.getAllThreadIds());
		long bcValue = 0;
		if (threads != null) {
			for (ThreadInfo t : threads) {
				if (t != null && Thread.State.BLOCKED.equals(t.getThreadState())) {
					bcValue++;
				}
			}
			item(MetricsTypeEnum.THREAD_BLOCKED, replicaName, metricsList, bcValue);
		}

		long[] dlThreads = txb.findDeadlockedThreads();
		long dlSize = (long) (dlThreads == null ? 0 : dlThreads.length);
		item(MetricsTypeEnum.THREAD_DEADLOCKED, replicaName, metricsList, dlSize);
	}
	
	private static void item(MetricsTypeEnum metricsType, String replicaName,
			List<Metrics> metricsList, long metricsValue) {
		Metrics used = new Metrics();
		used.setReplicaName(replicaName);
		used.setMetricsType(metricsType.getCode());
		used.setMetricsValue(metricsValue);
		metricsList.add(used);
	}
}