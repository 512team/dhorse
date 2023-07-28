package org.dhorse.api.event;

/**
 * 部署通知模型
 * 
 * @author Dahi
 *
 */
public class DeploymentMessage extends BuildVersionMessage {

	/**
	 * 环境标识
	 * <p>
	 * 开发者可以根据该值来判断当前事件来自哪个环境
	 */
	private String envTag;

	/**
	 * 审批人
	 */
	private String approver;

	public String getEnvTag() {
		return envTag;
	}

	public void setEnvTag(String envTag) {
		this.envTag = envTag;
	}

	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}

}
