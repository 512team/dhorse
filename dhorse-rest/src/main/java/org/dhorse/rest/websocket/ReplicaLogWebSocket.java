package org.dhorse.rest.websocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.dhorse.rest.websocket.ssh.SSHContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.dsl.LogWatch;

/**
 * 
 * 副本日志
 * 
 * @author 天地之怪
 */
@Component
@ServerEndpoint("/replica/log/{appId}/{envId}/{replicaname}/{logintoken}")
public class ReplicaLogWebSocket extends AbstracWebSocket{

	private static final Logger logger = LoggerFactory.getLogger(ReplicaLogWebSocket.class);

	@OnOpen
	public void onOpen(@PathParam("appId") String appId,
			@PathParam("envId") String envId,
			@PathParam("replicaname") String replicaName,
			@PathParam("logintoken") String loginToken,
			Session session) {
		SSHContext ssContext = sshContext(loginToken, appId, envId, replicaName);
		LogWatch watch = ssContext.getClient()
				.pods()
				.inNamespace(ssContext.getNamespace())
				.withName(replicaName)
	    		.tailingLines(2000)
	    		.watchLog();
		ssContext.setWatch(watch);
		ssContext.setSession(session);
		try {
			WebSocketCache.putReplicaLog(session.getId(), ssContext);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(watch.getOutput()));
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
		logger.info("Close socket, id : {}", session.getId());
		WebSocketCache.removeReplicaLog(session.getId());
	}
}