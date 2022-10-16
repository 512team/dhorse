package org.dhorse.api.vo;

/**
 * 分支信息
 * 
 * @author Dahai 2021-09-08
 */
public class ProjectBranch extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 分支名
	 */
	private String branchName;

	/**
	 * 合并状态，0：未合并，1：已合并
	 */
	private Integer mergedStatus;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public Integer getMergedStatus() {
		return mergedStatus;
	}

	public void setMergedStatus(Integer mergedStatus) {
		this.mergedStatus = mergedStatus;
	}

}