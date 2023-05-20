package org.dhorse.infrastructure.repository.source;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public class DefaultHikariDataSource extends HikariDataSource{
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultHikariDataSource.class);
	
	public DefaultHikariDataSource() {
		try {
			logger.info("Starting embedded h2 server...");
			Server.createTcpServer("-tcp", "-tcpAllowOthers","-ifNotExists", "-tcpPort",
					"59539").start();
			logger.info("The embedded db server started successfully");
		} catch (Exception e) {
			logger.error("Failed to start embedded h2 server", e);
		}
	}
}
