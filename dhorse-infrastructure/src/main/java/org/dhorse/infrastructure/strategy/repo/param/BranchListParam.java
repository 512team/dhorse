package org.dhorse.infrastructure.strategy.repo.param;

public class BranchListParam {

	private String appIdOrPath;

	private String branchName;

	public String getAppIdOrPath() {
		return appIdOrPath;
	}

	public void setAppIdOrPath(String appIdOrPath) {
		this.appIdOrPath = appIdOrPath;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

}
