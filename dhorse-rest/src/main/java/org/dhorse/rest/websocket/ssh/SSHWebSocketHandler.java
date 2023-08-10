package org.dhorse.rest.websocket.ssh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

/**
 * 
 * 副本终端
 * 
 * @author 天地之怪
 */
@Component
public class SSHWebSocketHandler implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(SSHWebSocketHandler.class);

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
			
			KubernetesClient client = client(appEnvClusterContext.getClusterPO().getClusterUrl(),
					appEnvClusterContext.getClusterPO().getAuthToken());
			String namespace = appEnvClusterContext.getAppEnvPO().getNamespaceName();
			
			ThreadPoolUtils.terminal(new Runnable() {
				@Override
				public void run() {
					if(!doConnect(client, namespace, sshConnectInfo, replicaTerminalParam, session, "sh")) {
						doConnect(client, namespace, sshConnectInfo, replicaTerminalParam, session, "sh");
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

	private boolean doConnect(KubernetesClient client, String namespace, SSHConnectContext sshConnectInfo,
			EnvReplicaTerminalParam replicaTerminalParam, WebSocketSession session, String bash) {
		String cmdOutput = execCommandOnPod(sshConnectInfo, client, replicaTerminalParam.getReplicaName(), namespace, bash);
		if(cmdOutput.contains("executable file not found")) {
			return false;
		}
		TextMessage textMessage = new TextMessage(cmdOutput);
				sendMessage(session, textMessage);
		return true;
	}
	
	public void close(WebSocketSession session) {
		SSHConnectContext sshConnectInfo = sshMap.get(session.getId());
		if (sshConnectInfo == null) {
			return;
		}
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
				|| sshConnectInfo.getWatch() == null) {
			return;
		}
		
		OutputStream outputStream = sshConnectInfo.getWatch().getInput();
		outputStream.write(command.getBytes());
		outputStream.flush();
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
	
	public static String execCommandOnPod(SSHConnectContext sshConnectInfo, KubernetesClient client, String podName, String namespace, String... cmd) {
		CompletableFuture<String> data = new CompletableFuture<>();
		ExecWatch execWatch = execCmd(client, podName, namespace, data, cmd);
		sshConnectInfo.setWatch(execWatch);
		try {
			return data.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static ExecWatch execCmd(KubernetesClient client, String podName, String namespace, CompletableFuture<String> data, String... command) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return client.pods().inNamespace(namespace).withName(podName).writingOutput(baos).writingError(baos)
				.usingListener(new SimpleListener(data, baos)).exec(command);
	}

	static class SimpleListener implements ExecListener {

		private CompletableFuture<String> data;
		private ByteArrayOutputStream baos;

		public SimpleListener(CompletableFuture<String> data, ByteArrayOutputStream baos) {
			this.data = data;
			this.baos = baos;
		}

		@Override
		public void onOpen() {
			System.out.println("Reading data... ");
		}

		@Override
		public void onFailure(Throwable t, Response failureResponse) {
			System.err.println(t.getMessage());
			data.completeExceptionally(t);
		}

		@Override
		public void onClose(int code, String reason) {
			System.out.println("Exit with: " + code + " and with reason: " + reason);
			data.complete(baos.toString());
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