package org.dhorse.rest.websocket;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dhorse.api.enums.LogTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.param.LogRecordParam;
import org.dhorse.infrastructure.repository.LogRecordRepository;
import org.dhorse.infrastructure.repository.po.LogRecordPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * 部署历史日志
 * 
 * @author 天地之怪
 */
@Component
public class DeploymentDetailLogWebSocket implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentDetailLogWebSocket.class);

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (message instanceof TextMessage) {
			sendMessage(((TextMessage) message).getPayload(), session);
		} else if (message instanceof BinaryMessage) {

		} else if (message instanceof PongMessage) {

		} else {
			logger.info("Unexpected WebSocket message type: {}", message);
		}
	}

	public void sendMessage(String payload, WebSocketSession session) {
		JsonNode param = JsonUtils.parseToNode(payload);
		SysUserApplicationService sysUserApplicationService = SpringBeanContext
				.getBean(SysUserApplicationService.class);
		LoginUser loginUser = sysUserApplicationService.queryLoginUserByToken(param.get("loginToken").asText());
		String deploymentDetailId = param.get("deploymentDetailId").asText();
		String appId = param.get("appId").asText();
		deploymentLog(loginUser, deploymentDetailId, appId, session);
	}

	public void deploymentLog(LoginUser loginUser, String id, String appId, WebSocketSession session) {
		if (loginUser == null) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		LogRecordRepository repository = SpringBeanContext.getBean(LogRecordRepository.class);
		LogRecordParam bizParam = new LogRecordParam();
		bizParam.setAppId(appId);
		bizParam.setBizId(id);
		bizParam.setLogType(LogTypeEnum.DEPLOY_ENV.getCode());
		List<LogRecordPO> records = repository.list(bizParam, null);
		LogRecordPO lastRecord = null;
		if(!CollectionUtils.isEmpty(records)) {
			try {
				for(LogRecordPO r : records) {
					Matcher m = Pattern.compile(Constants.CRLF).matcher(r.getContent());
					session.sendMessage(new TextMessage(m.replaceAll("<br/>")));
				}
			} catch (Exception e) {
				logger.error("Failed to write log to socket", e);
			}
			lastRecord = records.get(records.size() - 1);
		}else {
			//如果没有日志，则构建空日志以帮助WebSocketCache.removeExpired回收session
			lastRecord = new LogRecordPO();
			lastRecord.setAppId(appId);
			lastRecord.setBizId(id);
			lastRecord.setLogType(LogTypeEnum.DEPLOY_ENV.getCode());
			lastRecord.setCreationTime(new Date());
			
		}
		WebSocketCache.put(session, lastRecord);
	}

	private void close(WebSocketSession session) {
		WebSocketCache.remove(session);
		try {
			session.close();
		} catch (IOException e) {
			logger.error("Failed to close websocket", e);
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		close(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		close(session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

}