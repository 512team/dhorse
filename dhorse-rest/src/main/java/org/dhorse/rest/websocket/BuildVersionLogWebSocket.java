package org.dhorse.rest.websocket;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.input.Tailer;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.vo.DeploymentVersion;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.param.DeploymentVersionParam;
import org.dhorse.infrastructure.repository.DeploymentVersionRepository;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.rest.component.ApplicationReadyListner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
 * 构建版本日志
 * 
 * @author 天地之怪
 */
@Component
public class BuildVersionLogWebSocket implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(BuildVersionLogWebSocket.class);

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
		String id = param.get("id").asText();
		String projectId = param.get("projectId").asText();
		log(loginUser, id, projectId, session);
	}

	public void log(LoginUser loginUser, String id, String projectId, WebSocketSession session) {
		if (loginUser == null) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		DeploymentVersionRepository deploymentVersionRepository = SpringBeanContext
				.getBean(DeploymentVersionRepository.class);
		DeploymentVersionParam deploymentVersionParam = new DeploymentVersionParam();
		deploymentVersionParam.setProjectId(projectId);
		deploymentVersionParam.setId(id);
		DeploymentVersion deploymentVersion = deploymentVersionRepository.query(loginUser,
				deploymentVersionParam);
		ComponentConstants componentConstants = SpringBeanContext.getBean(ComponentConstants.class);
		Tailer tailer = new Tailer(new File(Constants.buildVersionLogFile(componentConstants.getLogPath(),
				deploymentVersion.getCreationTime(), deploymentVersion.getId())),
				new LogTailerListener(session), 1000);
		ApplicationReadyListner.WEB_SOCKET_CACHE.put(session, tailer);
		tailer.run();
	}

	private void close(WebSocketSession session) {
		Tailer tailer = ApplicationReadyListner.WEB_SOCKET_CACHE.remove(session);
		if (tailer != null) {
			tailer.stop();
		}
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