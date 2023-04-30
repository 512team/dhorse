package org.dhorse.rest.component;

import org.dhorse.rest.websocket.BuildVersionLogWebSocket;
import org.dhorse.rest.websocket.DeploymentDetailLogWebSocket;
import org.dhorse.rest.websocket.TerminalWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * websocket配置
 * 
 * @Author: Dahai
 */
@Configuration
@EnableWebSocket
public class WebSSHWebSocketConfig implements WebSocketConfigurer {
	
	@Autowired
	private TerminalWebSocketHandler terminalWebSocketHandler;
	
	@Autowired
	private DeploymentDetailLogWebSocket deploymentDetailLogWebSocket;
	
	@Autowired
	private BuildVersionLogWebSocket buildVersionLogWebSocket;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
		webSocketHandlerRegistry.addHandler(terminalWebSocketHandler, "/terminal")
			.addHandler(deploymentDetailLogWebSocket, "/deployment/log")
			.addHandler(buildVersionLogWebSocket, "/build/log")
			.setAllowedOrigins("*");
	}
}
