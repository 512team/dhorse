package org.dhorse.infrastructure.strategy.repo.param;

public class BranchListParam {

	private String projectIdOrPath;

	private String branchName;

	public String getProjectIdOrPath() {
		return projectIdOrPath;
	}

	public void setProjectIdOrPath(String projectIdOrPath) {
		this.projectIdOrPath = projectIdOrPath;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

}
