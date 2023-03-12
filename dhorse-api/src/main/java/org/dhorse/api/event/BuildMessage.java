package org.dhorse.api.event;

/**
 * 构建版本通知模型
 * 
 * @author Dahi
 *
 */
public class BuildMessage {

	/**
	 * 提交人
	 */
	private String submitter;

	/**
	 * 分支
	 */
	private String branchName;

	/**
	 * tag
	 */
	private String tagName;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 版本名称
	 */
	private String verionName;

	/**
	 * 状态
	 * <p>
	 * 构建事件，参见：org.dhorse.api.enums.BuildStatusEnum
	 * <P>
	 * 部署事件，参见：org.dhorse.api.enums.DeploymentStatusEnum
	 */
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
