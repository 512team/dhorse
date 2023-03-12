package org.dhorse.api.event;

/**
 * 构建版本通知模型
 * 
 * @author Dahi
 *
 */
public class BuildMessage {

	private String submitter;

	private String branchName;

	private String tagName;

	private String appName;

	private String verionName;

	// 构建状态，1：构建成功，2：构建失败
	private Integer status;

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getVerionName() {
		return verionName;
	}

	public void setVerionName(String verionName) {
		this.verionName = verionName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
