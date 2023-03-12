package org.dhorse.api.event;

/**
 * 部署通知模型
 * 
 * @author Dahi
 *
 */
public class DeploymentMessage extends BuildMessage {

	/**
	 * 审批人
	 */
	private String approver;

	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}

}
