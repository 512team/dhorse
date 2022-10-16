package org.dhorse.rest.websocket;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.input.Tailer;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.vo.DeploymentDetail;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.param.DeploymentDetailParam;
import org.dhorse.infrastructure.repository.DeploymentDetailRepository;
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
		String projectId = param.get("projectId").asText();
		deploymentLog(loginUser, deploymentDetailId, projectId, session);
	}

	public void deploymentLog(LoginUser loginUser, String deploymentDetailId, String projectId, WebSocketSession session) {
		if (loginUser == null) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		DeploymentDetailRepository deploymentDetailRepository = SpringBeanContext
				.getBean(DeploymentDetailRepository.class);
		DeploymentDetailParam deploymentDetailParam = new DeploymentDetailParam();
		deploymentDetailParam.setProjectId(projectId);
		deploymentDetailParam.setId(deploymentDetailId);
		DeploymentDetail deploymentDetail = deploymentDetailRepository.query(loginUser,
				deploymentDetailParam);
		ComponentConstants componentConstants = SpringBeanContext.getBean(ComponentConstants.class);
		Tailer tailer = new Tailer(new File(Constants.deploymentLogFile(componentConstants.getLogPath(),
				deploymentDetail.getStartTime(), deploymentDetail.getId())),
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