package org.dhorse.api.vo;

/**
 * 标签信息
 * 
 * @author 无双
 */
public class AppTag extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 分支名
	 */
	private String tagName;

	/**
	 * 最后提交信息
	 */
	private String commitMessage;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

}