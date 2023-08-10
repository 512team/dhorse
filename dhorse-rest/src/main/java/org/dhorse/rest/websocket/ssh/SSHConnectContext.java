package org.dhorse.rest.websocket.ssh;

import org.springframework.web.socket.WebSocketSession;

import io.fabric8.kubernetes.client.dsl.ExecWatch;

public class SSHConnectContext {

	private WebSocketSession webSocketSession;
	private ExecWatch watch;

	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}

	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public ExecWatch getWatch() {
		return watch;
	}

	public void setWatch(ExecWatch watch) {
		this.watch = watch;
	}

}
