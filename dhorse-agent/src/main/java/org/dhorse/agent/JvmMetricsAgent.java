package org.dhorse.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 采集Jvm信息的agent
 * <p>
 * 启动方式：java -javaagent:dhorse-agent.jar
 * 
 * @author 无双
 */
public class JvmMetricsAgent {

	private static final Logger logger = Logger.getLogger("JvmMetricsAgent");
	
	private static final String DHORSE_SERVER_URL_FILE_PATH = "/usr/local/data/dhorse-server-url";
	
	private static final List<String> DHORSE_URL = new CopyOnWriteArrayList<>();
	
    private static long lastModifiedTime;

	public static void premain(String args, Instrumentation inst) {
		
		loadDHorseIp();
		
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warning(String.format("Failed to get localhost name, message: %s", e));
		}
		
		final String replicaName = hostName;
		ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
		scheduled.scheduleAtFixedRate(() -> {
			if(DHORSE_URL.size() == 0) {
				//如果没有dhorse服务器的url，则重新读取DHorse的Url一次
				loadDHorseIp();
			}
			
			//如果仍然没有dhorse服务器的url，则不上报
			if(DHORSE_URL.size() == 0) {
				logger.warning(String.format("Failed to push metrics data, none dhorse server exists"));
				return;
			}
			
			List<Metrics> metricsList = new ArrayList<>(16);
			memoryHeap(replicaName, metricsList);
			memoryPool(replicaName, metricsList);
			gc(replicaName, metricsList);
			thread(replicaName, metricsList);
			
			if(pushMetrics(metricsList)) {
				//如果没有上报成功，重新读取DHorse的Url并重新上报一次
				loadDHorseIp();
				pushMetrics(metricsList);
			}
		}, 0, 10, TimeUnit.SECONDS);
		
		//监控文件变化
		watchFile();
	}

	private static void loadDHorseIp() {
		try(InputStream in = new FileInputStream(DHORSE_SERVER_URL_FILE_PATH)){
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			String ipStr = new String(buffer, "UTF-8");
			DHORSE_URL.clear();
			if(ipStr != null && !"".equals(ipStr)) {
				String[] ips = ipStr.split(",");
				for(String ip : ips) {
					DHORSE_URL.add("http://" + ip + "/app/env/replica/metrics/add");
				}
			}
		} catch (Exception e) {
			logger.warning(String.format("Failed to load dhorse url, message: %s", e));
		}
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
	
	public static boolean pushMetrics(List<Metrics> metricsList) {
		String url = DHORSE_URL.get(new Random().nextInt(DHORSE_URL.size()));
		return HttpUtils.post(url, metricsList.toString());
	}
	
//	public static void watchFile(String path) {
//		// 获取当前文件系统的监控对象
//		try (WatchService service = FileSystems.getDefault().newWatchService()) {
//			// 获取文件目录下的Path对象注册到 watchService中, 监听的事件类型，有创建，删除，以及修改
//			Paths.get(path).register(service, StandardWatchEventKinds.ENTRY_CREATE,
//					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
//
//			while (true) {
//				// 获取可用key.没有可用的就wait
//				WatchKey key = service.take();
//				for (WatchEvent<?> event : key.pollEvents()) {
//					System.out.println(String.format("The file in the %s directory has %s", path, event.kind().name().toLowerCase()));
//					loadDHorseIp();
//				}
//				// 重置，这一步很重要，否则当前的key就不再会获取将来发生的事件
//				boolean valid = key.reset();
//				// 失效状态，退出监听
//				if (!valid) {
//					break;
//				}
//			}
//		} catch (IOException | InterruptedException e) {
//			System.out.println(String.format("Failed to watch file, message: %s", e));
//		}
//	}
	
    public static void watchFile() {
        File file = new File(DHORSE_SERVER_URL_FILE_PATH);
        lastModifiedTime = file.lastModified();
        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (file.lastModified() > lastModifiedTime || DHORSE_URL.size() == 0) {
                	logger.info(String.format("The file %s has changed", DHORSE_SERVER_URL_FILE_PATH));
					loadDHorseIp();
                    lastModifiedTime = file.lastModified();
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}