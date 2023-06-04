package org.dhorse.api.response.model;

import java.io.Serializable;

/**
 * 环境生命周期配置
 * 
 * @author Dahai
 */
public class EnvLifecycle extends BaseDto {

	private static final long serialVersionUID = 1L;

	private Item postStart;

	private Item preStop;

	public Item getPostStart() {
		return postStart;
	}

	public void setPostStart(Item postStart) {
		this.postStart = postStart;
	}

	public Item getPreStop() {
		return preStop;
	}

	public void setPreStop(Item preStop) {
		this.preStop = preStop;
	}

	public static enum HookTypeEnum {

		POST_START(1, "启动后执行"),
		PRE_STOP(2, "销毁前执行");

		private Integer code;

		private String value;

		private HookTypeEnum(Integer code, String value) {
			this.code = code;
			this.value = value;
		}

		public Integer getCode() {
			return code;
		}

		public String getValue() {
			return value;
		}
	}

	public static class Item implements Serializable {

		private static final long serialVersionUID = 1L;

		/**
		 * 编号
		 */
		private String id;

		/**
		 * 应用编号
		 */
		private String appId;

		/**
		 * 环境编号
		 */
		private String envId;

		/**
		 * 钩子类型
		 */
		private Integer hookType;

		/**
		 * 执行类型
		 */
		private Integer actionType;

		/**
		 * 执行内容
		 */
		private String action;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getAppId() {
			return appId;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}

		public String getEnvId() {
			return envId;
		}

		public void setEnvId(String envId) {
			this.envId = envId;
		}

		public Integer getHookType() {
			return hookType;
		}

		public void setHookType(Integer hookType) {
			this.hookType = hookType;
		}

		public Integer getActionType() {
			return actionType;
		}

		public void setActionType(Integer actionType) {
			this.actionType = actionType;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}
	}
}