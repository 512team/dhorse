package org.dhorse.rest.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dhorse.api.param.app.env.replica.EnvReplicaTerminalParam;
import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.context.AppEnvClusterContext;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;

/**
 * 
 * 副本终端
 * 
 * @author 天地之怪
 */
@Component
public class TerminalWebSocketHandler implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(TerminalWebSocketHandler.class);

	private static Map<String, SSHConnectContext> sshMap = new ConcurrentHashMap<>();

	@Autowired
	private SysUserApplicationService sysUserApplicationService;
	
	@Autowired
	private EnvReplicaApplicationService replicaApplicationService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		SSHConnectContext sshConnectInfo = new SSHConnectContext();
		sshConnectInfo.setWebSocketSession(session);
		sshMap.put(session.getId(), sshConnectInfo);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (message instanceof TextMessage) {
			terminal(((TextMessage) message).getPayload(), session);
		} else if (message instanceof BinaryMessage) {

		} else if (message instanceof PongMessage) {

		} else {
			logger.info("Unexpected WebSocket message type: {}", message);
		}

	}

	public void terminal(String payload, WebSocketSession session) {
		EnvReplicaTerminalParam replicaTerminalParam = JsonUtils.parseToObject(payload, EnvReplicaTerminalParam.class);
		SSHConnectContext sshConnectInfo = sshMap.get(session.getId());
		if ("connect".equals(replicaTerminalParam.getOperate())) {
			
			LoginUser loginUser = sysUserApplicationService
					.queryLoginUserByToken(replicaTerminalParam.getLoginToken());
			if(loginUser == null) {
				sendMessage(session, "用户未登录".getBytes());
				return;
			}
			
			AppEnvClusterContext appEnvClusterContext = null;
			try {
				appEnvClusterContext = replicaApplicationService
						.queryCluster(replicaTerminalParam.getReplicaName(), loginUser);
			}catch(ApplicationException e) {
				sendMessage(session, e.getMessage().getBytes());
				return;
			}
			
			ApiClient apiClient = apiClient(appEnvClusterContext.getClusterPO().getClusterUrl(),
					appEnvClusterContext.getClusterPO().getAuthToken());
			Exec exec = new Exec(apiClient);
			String namespace = appEnvClusterContext.getAppEnvPO().getNamespaceName();
			
			ThreadPoolUtils.terminal(new Runnable() {
				@Override
				public void run() {
					if(!doConnect(exec, namespace, sshConnectInfo, replicaTerminalParam, session, "bash")) {
						doConnect(exec, namespace, sshConnectInfo, replicaTerminalParam, session, "sh");
					}
				}
			});
		} else if ("command".equals(replicaTerminalParam.getOperate())) {
			try {
				transToSSH(sshConnectInfo, replicaTerminalParam.getCommand());
			} catch (IOException e) {
				logger.error("websocket error.", e);
				close(session);
			}
		} else {
			logger.warn("Unsupported operation, websocket session id : {}", session.getId());
			close(session);
		}
	}

	private boolean doConnect(Exec exec, String namespace, SSHConnectContext sshConnectInfo,
			EnvReplicaTerminalParam replicaTerminalParam, WebSocketSession session, String bash) {
		try {
			Process process = exec.exec(namespace, replicaTerminalParam.getReplicaName(),
					new String[]{ bash }, true, true);
			sshConnectInfo.setProcess(process);
			InputStream inputStream = process.getInputStream();
			try {
				byte[] buffer = new byte[1024];
				int i = 0;
				while ((i = inputStream.read(buffer)) != -1) {
					TextMessage textMessage = new TextMessage(Arrays.copyOfRange(buffer, 0, i));
					if(textMessage.getPayload().contains("executable file not found")) {
						return false;
					}
					sendMessage(session, textMessage);
				}
			} finally {
				process.destroy();
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} catch (ApiException | IOException e) {
			logger.error("websocket error.", e);
		}
		return true;
	}
	
	public void close(WebSocketSession session) {
		SSHConnectContext sshConnectInfo = sshMap.get(session.getId());
		if (sshConnectInfo == null) {
			return;
		}
//		if(sshConnectInfo.getProcess() != null) {
//			sshConnectInfo.getProcess().destroy();
//		}
		sshMap.remove(session.getId());
	}

	public void sendMessage(WebSocketSession session, byte[] buffer){
		try {
			session.sendMessage(new TextMessage(buffer));
		} catch (IOException e) {
			logger.error("Failed to send websocket buffer", e);
		}
	}
	
	public void sendMessage(WebSocketSession session, TextMessage message){
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			logger.error("Failed to send websocket message", e);
		}
	}

	private void transToSSH(SSHConnectContext sshConnectInfo, String command) throws IOException {
		if(!sshConnectInfo.getWebSocketSession().isOpen()
				|| sshConnectInfo.getProcess() == null
				|| !sshConnectInfo.getProcess().isAlive()) {
			return;
		}
		
		OutputStream outputStream = sshConnectInfo.getProcess().getOutputStream();
		outputStream.write(command.getBytes());
		outputStream.flush();
	}

	private ApiClient apiClient(String basePath, String accessToken) {
		ApiClient apiClient = new ClientBuilder().setBasePath(basePath).setVerifyingSsl(false)
				.setAuthentication(new AccessTokenAuthentication(accessToken)).build();
		return apiClient;
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		
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