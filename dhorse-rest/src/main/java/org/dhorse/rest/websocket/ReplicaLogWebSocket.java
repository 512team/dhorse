package org.dhorse.rest.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * 副本日志
 * 
 * @author 天地之怪
 */
@Component
@ServerEndpoint("/replica/log/{replicaname}/{logintoken}")
public class ReplicaLogWebSocket {

	private static final Logger logger = LoggerFactory.getLogger(ReplicaLogWebSocket.class);

	private static Map<String, InputStream> streams = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(@PathParam("replicaname") String replicaName, @PathParam("logintoken") String loginToken,
			Session session) {
		SysUserApplicationService sysUserApplicationService = SpringBeanContext
				.getBean(SysUserApplicationService.class);
		LoginUser loginUser = sysUserApplicationService.queryLoginUserByToken(loginToken);
		EnvReplicaApplicationService replicaApplicationService = SpringBeanContext
				.getBean(EnvReplicaApplicationService.class);
		InputStream is = replicaApplicationService.streamPodLog(loginUser, replicaName);
		if (is == null) {
			return;
		}

		try {
			streams.put(session.getId(), is);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = buffer.readLine()) != null) {
				session.getBasicRemote().sendText(line + "</br>");
			}
		} catch (Exception e) {
			logger.error("Websocket error", e);
		}
	}

	@OnClose
	public void onClose(Session session) {
		logger.info("close socket, id : {}", session.getId());
		InputStream is = streams.remove(session.getId());
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				logger.error("Failed to close websocket", e);
			}
		}
		try {
			session.close();
		} catch (IOException e) {
			logger.error("Failed to close websocket", e);
		}
	}

}