package org.dhorse.api.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GlobalConfigAgg implements Serializable {

	private static final long serialVersionUID = 1L;

	private CodeRepo codeRepo;

	private ImageRepo imageRepo;

	private Ldap ldap;

	private Maven maven;

	private Map<String, TraceTemplate> traceTemplates = new HashMap<>();

	public Maven getMaven() {
		return maven;
	}

	public void setMaven(Maven maven) {
		this.maven = maven;
	}

	public CodeRepo getCodeRepo() {
		return codeRepo;
	}

	public void setCodeRepo(CodeRepo codeRepo) {
		this.codeRepo = codeRepo;
	}

	public ImageRepo getImageRepo() {
		return imageRepo;
	}

	public void setImageRepo(ImageRepo imageRepo) {
		this.imageRepo = imageRepo;
	}

	public Ldap getLdap() {
		return ldap;
	}

	public void setLdap(Ldap ldap) {
		this.ldap = ldap;
	}

	public TraceTemplate getTraceTemplate(String id) {
		return traceTemplates.get(id);
	}

	public void setTraceTemplate(String id, TraceTemplate traceTemplate) {
		this.traceTemplates.put(id, traceTemplate);
	}

	public static abstract class BaseGlobalConfig implements Serializable {

		private static final long serialVersionUID = 1L;

		private String id;

		private Integer itemType;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Integer getItemType() {
			return itemType;
		}

		public void setItemType(Integer itemType) {
			this.itemType = itemType;
		}

	}

	public static class ImageRepo extends BaseGlobalConfig {

		private static final long serialVersionUID = 1L;

		private String type;

		private String url;

		private String authUser;

		private String authPassword;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getAuthUser() {
			return authUser;
		}

		public void setAuthUser(String authUser) {
			this.authUser = authUser;
		}

		public String getAuthPassword() {
			return authPassword;
		}

		public void setAuthPassword(String authPassword) {
			this.authPassword = authPassword;
		}

	}

	public static class CodeRepo extends BaseGlobalConfig {

		private static final long serialVersionUID = 1L;

		private String type;

		private String url;

		private String authToken;

		private String authUser;

		private String authPassword;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getAuthToken() {
			return authToken;
		}

		public void setAuthToken(String authToken) {
			this.authToken = authToken;
		}

		public String getAuthUser() {
			return authUser;
		}

		public void setAuthUser(String authUser) {
			this.authUser = authUser;
		}

		public String getAuthPassword() {
			return authPassword;
		}

		public void setAuthPassword(String authPassword) {
			this.authPassword = authPassword;
		}

	}

	public static class Ldap extends BaseGlobalConfig {

		private static final long serialVersionUID = 1L;

		private Integer enable;

		private String url;

		private String adminDn;

		private String adminPassword;

		private String searchBaseDn;

		public Integer getEnable() {
			return enable;
		}

		public void setEnable(Integer enable) {
			this.enable = enable;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getAdminDn() {
			return adminDn;
		}

		public void setAdminDn(String adminDn) {
			this.adminDn = adminDn;
		}

		public String getAdminPassword() {
			return adminPassword;
		}

		public void setAdminPassword(String adminPassword) {
			this.adminPassword = adminPassword;
		}

		public String getSearchBaseDn() {
			return searchBaseDn;
		}

		public void setSearchBaseDn(String searchBaseDn) {
			this.searchBaseDn = searchBaseDn;
		}

	}

	public static class Maven extends BaseGlobalConfig {

		private static final long serialVersionUID = 1L;

		/**
		 * java home
		 */
		private String javaHome;

		/**
		 * java版本
		 */
		private String javaVersion;

		/**
		 * maven仓库地址
		 */
		private String mavenRepoUrl;

		public String getJavaHome() {
			return javaHome;
		}

		public void setJavaHome(String javaHome) {
			this.javaHome = javaHome;
		}

		public String getJavaVersion() {
			return javaVersion;
		}

		public void setJavaVersion(String javaVersion) {
			this.javaVersion = javaVersion;
		}

		public String getMavenRepoUrl() {
			return mavenRepoUrl;
		}

		public void setMavenRepoUrl(String mavenRepoUrl) {
			this.mavenRepoUrl = mavenRepoUrl;
		}
	}

	public static class TraceTemplate extends BaseGlobalConfig {

		private static final long serialVersionUID = 1L;

		/**
		 * 模板名称
		 */
		private String name;

		/**
		 * 服务地址，如：127.0.0.1:1800
		 */
		private String serverUrl;

		/**
		 * 语言类型
		 */
		private Integer languageType;

		/**
		 * Agent版本
		 */
		private String agentVersion;

		/**
		 * Agent镜像
		 */
		private String agentImage;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getServerUrl() {
			return serverUrl;
		}

		public void setServerUrl(String serverUrl) {
			this.serverUrl = serverUrl;
		}

		public String getAgentImage() {
			return agentImage;
		}

		public void setAgentImage(String agentImage) {
			this.agentImage = agentImage;
		}

		public Integer getLanguageType() {
			return languageType;
		}

		public void setLanguageType(Integer languageType) {
			this.languageType = languageType;
		}

		public String getAgentVersion() {
			return agentVersion;
		}

		public void setAgentVersion(String agentVersion) {
			this.agentVersion = agentVersion;
		}

	}
}
