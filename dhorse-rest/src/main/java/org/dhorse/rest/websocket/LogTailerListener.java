package org.dhorse.rest.websocket;

import java.io.IOException;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 
 * 读取日志文件
 * 
 * @author 天地之怪
 */
public class LogTailerListener implements TailerListener {

	private static final Logger logger = LoggerFactory.getLogger(LogTailerListener.class);

	private WebSocketSession session;

	public LogTailerListener(WebSocketSession session) {
		this.session = session;
	}

	@Override
	public void init(Tailer tailer) {

	}

	@Override
	public void fileNotFound() {

	}

	@Override
	public void fileRotated() {

	}

	@Override
	public void handle(String line) {
		try {
			session.sendMessage(new TextMessage(line + "</br>"));
		} catch (IOException e) {
			logger.error("Failed to print log.", e);
		}
	}

	@Override
	public void handle(Exception ex) {

	}

}
