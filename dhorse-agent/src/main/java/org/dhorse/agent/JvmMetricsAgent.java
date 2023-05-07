package org.dhorse.agent;

import java.lang.instrument.Instrumentation;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 采集Jvm信息的agent
 * <p>
 * 启动方式：java
 * -javaagent:dhorse-agent.jar=http://dhost_host:port/env/replica/jvmMetrics
 * 
 * @author 无双
 */
public class JvmMetricsAgent {

	private static final Logger logger = LoggerFactory.getLogger(JvmMetricsAgent.class);
	
	private static final ScheduledExecutorService SCHEDULED = Executors.newScheduledThreadPool(1);

	public static void premain(String args, Instrumentation inst) {
		
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Failed to get localhost name", e);
		}

		final String replicaName = hostName;
		SCHEDULED.scheduleAtFixedRate(() -> {
			List<Metrics> metricsList = new ArrayList<>(16);
			heapMemory(replicaName, metricsList);
			nonHeapMemory(replicaName, metricsList);
			metaspace(replicaName, metricsList);
			thread(replicaName, metricsList);
			HttpUtils.sendPost(args, metricsList.toString());
		}, 0, 5, TimeUnit.SECONDS);
	}

	private static void heapMemory(String replicaName, List<Metrics> metricsList) {
		MemoryUsage m = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		memory(MetricsTypeEnum.FirstType.HEAP_MEMORY, replicaName, metricsList, m);
	}

	private static void nonHeapMemory(String replicaName, List<Metrics> metricsList) {
		MemoryUsage m = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		memory(MetricsTypeEnum.FirstType.NON_HEAP_MEMORY, replicaName, metricsList, m);
	}

	private static void metaspace(String replicaName, List<Metrics> metricsList) {
		List<MemoryPoolMXBean> mpxbs = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean mp : mpxbs) {
			if (!"Metaspace".equalsIgnoreCase(mp.getName())) {
				continue;
			}
			memory(MetricsTypeEnum.FirstType.META_MEMORY, replicaName, metricsList, mp.getUsage());
		}
	}

	private static void memory(MetricsTypeEnum.FirstType firstType, String replicaName,
			List<Metrics> metricsList, MemoryUsage m) {
		Metrics used = new Metrics();
		used.setReplicaName(replicaName);
		used.setFirstType(firstType.getCode());
		used.setSecondType(MetricsTypeEnum.SecondType.Memory.USED.getCode());
		used.setMetricsValue(m.getUsed());
		metricsList.add(used);

		Metrics commited = new Metrics();
		commited.setReplicaName(replicaName);
		commited.setFirstType(firstType.getCode());
		commited.setSecondType(MetricsTypeEnum.SecondType.Memory.COMMITTED.getCode());
		commited.setMetricsValue(m.getCommitted());
		metricsList.add(commited);

		Metrics max = new Metrics();
		max.setReplicaName(replicaName);
		max.setFirstType(firstType.getCode());
		max.setSecondType(MetricsTypeEnum.SecondType.Memory.MAX.getCode());
		max.setMetricsValue(m.getMax());
		metricsList.add(max);
	}

	private static void thread(String replicaName, List<Metrics> metricsList) {
		ThreadMXBean txb = ManagementFactory.getThreadMXBean();

		Metrics c = new Metrics();
		c.setReplicaName(replicaName);
		c.setFirstType(MetricsTypeEnum.FirstType.THREAD.getCode());
		c.setSecondType(MetricsTypeEnum.SecondType.Thread.DEFAULT.getCode());
		c.setMetricsValue((long) txb.getThreadCount());
		metricsList.add(c);

		Metrics dc = new Metrics();
		dc.setReplicaName(replicaName);
		dc.setFirstType(MetricsTypeEnum.FirstType.THREAD.getCode());
		dc.setSecondType(MetricsTypeEnum.SecondType.Thread.DAEMON.getCode());
		dc.setMetricsValue((long) txb.getDaemonThreadCount());
		metricsList.add(dc);

		ThreadInfo[] threads = txb.getThreadInfo(txb.getAllThreadIds());
		long bcValue = 0;
		if (threads != null) {
			for (ThreadInfo t : threads) {
				if (t != null && Thread.State.BLOCKED.equals(t.getThreadState())) {
					bcValue++;
				}
			}
			Metrics bc = new Metrics();
			bc.setReplicaName(replicaName);
			bc.setFirstType(MetricsTypeEnum.FirstType.THREAD.getCode());
			bc.setSecondType(MetricsTypeEnum.SecondType.Thread.BLOCKED.getCode());
			bc.setMetricsValue(bcValue);
			metricsList.add(bc);
		}

		long[] dlThreads = txb.findDeadlockedThreads();
		Metrics dlc = new Metrics();
		dlc.setReplicaName(replicaName);
		dlc.setFirstType(MetricsTypeEnum.FirstType.THREAD.getCode());
		dlc.setSecondType(MetricsTypeEnum.SecondType.Thread.DEADLOCKED.getCode());
		dlc.setMetricsValue((long) (dlThreads == null ? 0 : dlThreads.length));
		metricsList.add(dlc);
	}
}