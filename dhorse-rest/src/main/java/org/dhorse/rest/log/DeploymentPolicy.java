package org.dhorse.rest.log;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dhorse.api.enums.LogTypeEnum;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.repository.LogRecordRepository;
import org.dhorse.infrastructure.repository.po.LogRecordPO;
import org.dhorse.infrastructure.utils.DeployContext;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.dhorse.rest.websocket.WebSocketCache;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * 部署策略
 * 
 * @author Dahai
 */
public class DeploymentPolicy implements LifeCycle {

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
				Integer logType = LogTypeEnum.valueOf(deployContext.getEventType().name()).getCode();
				LogRecordPO logRecord = new LogRecordPO();
				logRecord.setAppId(deployContext.getApp().getId());
				logRecord.setBizId(deployContext.getId());
				logRecord.setLogType(logType);
				logRecord.setContent(message);
				logRecord.setCreationTime(new Date());
				
				WebSocketSession session = WebSocketCache.get(deployContext.getId() + logType);
				if(session != null) {
					Matcher m = Pattern.compile("\r\n|\n|\r").matcher(message);
					session.sendMessage(new TextMessage(m.replaceAll("<br/>")));
					WebSocketCache.put(session, logRecord);
				}
				
				LogRecordRepository repository = SpringBeanContext.getBean(LogRecordRepository.class);
				repository.add(logRecord);
				return null;
			}
		};
		ThreadPoolUtils.writeLog(writeLog);
	}
}