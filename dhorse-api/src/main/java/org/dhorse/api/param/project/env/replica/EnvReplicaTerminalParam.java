package org.dhorse.api.param.project.env.replica;

import java.io.Serializable;

/**
 * 副本终端参数模型
 * 
 * @author Dahai
 * @date 2022-3-27 11:25:53
 */
public class EnvReplicaTerminalParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录token
	 */
	private String loginToken;

	/**
	 * 操作类型，0：连接，1：指令
	 */
	private String operate;

	/**
	 * 副本名称
	 */
	private String replicaName;

	/**
	 * 操作指令
	 */
	private String command;

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public String getOperate() {
		return operate;
	}

	public void setOperate(String operate) {
		this.operate = operate;
	}

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
