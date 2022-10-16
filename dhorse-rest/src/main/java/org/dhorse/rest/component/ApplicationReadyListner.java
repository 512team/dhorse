package org.dhorse.rest.component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * 
 * 启动监听
 * 
 * @author 天地之怪
 */
@Component
public class ApplicationReadyListner implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationReadyListner.class);

	@Autowired
	private ComponentConstants componentConstants;
	
	public static final Map<WebSocketSession, Tailer> WEB_SOCKET_CACHE = new ConcurrentHashMap<>();

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		ThreadPoolUtils.scheduled(() -> {
			clearLog();
		}, 12, TimeUnit.HOURS);
		
		ThreadPoolUtils.scheduled(() -> {
			doClose();
		}, 5, TimeUnit.SECONDS);
	}

	private void doClose() {
		for(Entry<WebSocketSession, Tailer> entity : WEB_SOCKET_CACHE.entrySet()) {
			//如果文件在90s内没有新数据写入，则认为文件以后就不会有变化了，同时关闭socket
			if(System.currentTimeMillis() - entity.getValue().getFile().lastModified() > 90 * 1000) {
				Tailer tailer = WEB_SOCKET_CACHE.remove(entity.getKey());
				if (tailer != null) {
					tailer.stop();
				}
				try {
					entity.getKey().close();
				} catch (IOException e) {
					logger.error("Failed to close websocket", e);
				}
			}
		}
	}
	
	private void clearLog() {
		File deploymentLogPath = new File(
				componentConstants.getLogPath() + Constants.DEPLOYED_RELATIVE_PATH);
		File[] logFiles = deploymentLogPath.listFiles();
		for (File file : logFiles) {
			LocalDate fileDate = LocalDate.parse(file.getName(),
					DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYYMMDD));
			LocalDate today = LocalDate.now();
			Period period = Period.between(fileDate, today);
			if (period.getDays() < Constants.DEPLOYED_LOG_EXIST_DAYS) {
				continue;
			}
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				logger.error("Failed to clear deployment file", e);
			}
		}
	}
}
