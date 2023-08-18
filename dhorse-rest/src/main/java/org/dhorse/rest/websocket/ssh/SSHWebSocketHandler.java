package org.dhorse.rest.websocket.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dhorse.api.param.app.env.replica.EnvReplicaTerminalParam;
import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.context.AppEnvClusterContext;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

/**
 * 
 * 副本终端
 * 
 * @author Dahi
 */
@Component
public class SSHWebSocketHandler implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(SSHWebSocketHandler.class);

	private static Map<String, SSHContext> sshMap = new ConcurrentHashMap<>();

	@Autowired
	private SysUserApplicationService sysUserApplicationService;
	
	@Autowired
	private EnvReplicaApplicationService replicaApplicationService;
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		SSHContext sshContext = new SSHContext();
		sshContext.setSession(session);
		sshMap.put(session.getId(), sshContext);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (message instanceof TextMessage) {
			terminal(((TextMessage) message).getPayload(), session);
		}
	}

	private void terminal(String payload, WebSocketSession session) {
		EnvReplicaTerminalParam replicaTerminalParam = JsonUtils.parseToObject(payload,
				EnvReplicaTerminalParam.class);
		SSHContext sshContext = sshMap.get(session.getId());
		if ("connect".equals(replicaTerminalParam.getOperate())) {
			
			LoginUser loginUser = sysUserApplicationService
					.queryLoginUserByToken(replicaTerminalParam.getLoginToken());
			if(loginUser == null) {
				sendMessage(session, "用户未登录");
				return;
			}
			
			AppEnvClusterContext appEnvClusterContext = null;
			try {
				appEnvClusterContext = replicaApplicationService
						.queryCluster(replicaTerminalParam.getReplicaName(), loginUser);
			}catch(ApplicationException e) {
				sendMessage(session, e.getMessage());
				return;
			}
			
			KubernetesClient client = client(appEnvClusterContext.getClusterPO().getClusterUrl(),
					appEnvClusterContext.getClusterPO().getAuthToken());
			String namespace = appEnvClusterContext.getAppEnvPO().getNamespaceName();
			sshContext.setNamespace(namespace);
			sshContext.setReplicaName(replicaTerminalParam.getReplicaName());
			sshContext.setClient(client);
			if(!doConnect(sshContext, "bash")) {
				doConnect(sshContext, "sh");
			}
		} else if ("command".equals(replicaTerminalParam.getOperate())) {
			transToSSH(sshContext, replicaTerminalParam.getCommand());
		} else {
			logger.warn("Unsupported operation, websocket session id : {}", session.getId());
			close(session);
		}
	}

	private boolean doConnect(SSHContext sshContext, String bash) {
		ExecWatch watch = sshContext.getClient().pods()
				.inNamespace(sshContext.getNamespace())
				.withName(sshContext.getReplicaName())
				.redirectingInput()
				.writingOutput(sshContext.getBaos())
				.writingError(sshContext.getBaos())
				.withTTY()
				.exec(bash);
		sshContext.setWatch(watch);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			//忽略
		}
		String output = output(sshContext);
		if(StringUtils.isBlank(output) || output.contains("executable file not found")) {
			return false;
		}
		sendMessage(sshContext.getSession(), output);
		return true;
	}
	
	private void close(WebSocketSession session) {
		SSHContext sshContext = sshMap.get(session.getId());
		if (sshContext == null) {
			return;
		}
		sshContext = sshMap.remove(session.getId());
		sshContext.setBaos(null);
		((ExecWatch)sshContext.getWatch()).close();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			//忽略
		}
		sshContext.getClient().close();
	}

	private void transToSSH(SSHContext sshContext, String command) {
		if(sshContext.getWatch() == null) {
			return;
		}
		OutputStream outputStream = ((ExecWatch)sshContext.getWatch()).getInput();
		try {
			outputStream.write(command.getBytes());
			outputStream.flush();
		} catch (IOException e) {
			logger.error("Failed to interact with terminal", e);
		}
		try {
			Thread.sleep(70);
		} catch (InterruptedException e) {
			//忽略
		}
		sendMessage(sshContext.getSession(), output(sshContext));
	}

	private KubernetesClient client(String basePath, String accessToken) {
		 Config config = new ConfigBuilder()
				 .withTrustCerts(true)
				 .withMasterUrl(basePath)
				 .withOauthToken(accessToken)
				 .build();
		return new KubernetesClientBuilder()
				.withConfig(config)
				.build();
	}
	
	private String output(SSHContext sshConnectInfo) {
		String content = sshConnectInfo.getBaos().toString();
		sshConnectInfo.getBaos().reset();
		return content;
	}
	
	private void sendMessage(WebSocketSession session, String output){
		try {
			session.sendMessage(new TextMessage(output));
		} catch (IOException e) {
			logger.error("Failed to send websocket message", e);
		}
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