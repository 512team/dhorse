package org.dhorse.infrastructure.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MysqlConfig {
	
	public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
	
	@Value("${mysql.enable:#{false}}")
	private boolean enable;
	
	@Value("${mysql.url:#{null}}")
	private String url;
	
	@Value("${mysql.user:#{null}}")
	private String user;
	
	@Value("${mysql.password:#{null}}")
	private String password;

	public boolean isEnable() {
		return enable;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
}