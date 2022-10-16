package org.dhorse.infrastructure.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.Callable;

import org.dhorse.infrastructure.utils.DeployContext;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * 部署策略。
 * 
 * @author Dahai 2021-11-16 17:24:46
 */
public class DeployingPolicy implements LifeCycle {

	private boolean start;

	@Override
	public void start() {
		this.start = true;
	}

	@Override
	public void stop() {
		this.start = false;
	}

	@Override
	public boolean isStarted() {
		return start;
	}

	/**
	 * 自定义处理日志逻辑
	 */
	public void handler(String message) {
		DeployContext deployContext = ThreadLocalUtils.getDeployContext();
		if (deployContext == null) {
			return;
		}
		Callable<Void> writeLog = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				File logFile = new File(deployContext.getLogFilePath());
				if (!logFile.getParentFile().exists()) {
					logFile.getParentFile().mkdirs();
				}
				if (!logFile.exists()) {
					try {
						logFile.createNewFile();
					} catch (IOException e) {
						// 如果这里创建失败，只打出异常堆栈，以免引起死循环
						e.printStackTrace();
					}
				}
				try (BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(deployContext.getLogFilePath(), true)))) {
					out.write(message);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		ThreadPoolUtils.writeLog(writeLog);
	}
}