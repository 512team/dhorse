package org.dhorse.api.param.app.branch;

import java.io.Serializable;

/**
 * 添加应用分支
 * 
 * @author Dahai
 */
public class AppBranchCreationParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 分支名
	 */
	private String branchName;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

}