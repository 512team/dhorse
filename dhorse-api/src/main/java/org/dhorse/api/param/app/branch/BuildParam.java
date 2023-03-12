package org.dhorse.api.param.app.branch;

import java.io.Serializable;

/**
 * 构建版本参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class BuildParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 分支编号
	 */
	private String branchName;

	/**
	 * 提交人
	 */
	private String submitter;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

}