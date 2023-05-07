package org.dhorse.infrastructure.strategy.cluster.model;

import java.util.Map;

public class DockerConfigJson {

	private Map<String, Auth> auths;

	public Map<String, Auth> getAuths() {
		return auths;
	}

	public void setAuths(Map<String, Auth> auths) {
		this.auths = auths;
	}

	public static class Auth {

		private String username;
		private String password;
		private String auth;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getAuth() {
			return auth;
		}

		public void setAuth(String auth) {
			this.auth = auth;
		}

	}

}
