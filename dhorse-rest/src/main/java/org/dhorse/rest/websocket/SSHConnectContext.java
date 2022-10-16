package org.dhorse.rest.websocket;

import org.springframework.web.socket.WebSocketSession;

public class SSHConnectContext {

	private WebSocketSession webSocketSession;
	private Process process;

	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}

	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

}
