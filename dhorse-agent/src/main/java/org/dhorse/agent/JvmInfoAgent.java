package org.dhorse.agent;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 采集Jvm信息的agent
 * <p>
 * 启动方式：java -javaagent:dhorse-agent.jar=http://dhost_host:port/env/replica/jvmMetrics
 * 
 * @author 猿码人
 */
public class JvmInfoAgent {

	public static void premain(String args, Instrumentation inst) {
		RuntimeMXBean mxb = ManagementFactory.getRuntimeMXBean();
		mxb.getVmVersion();
	}

}
