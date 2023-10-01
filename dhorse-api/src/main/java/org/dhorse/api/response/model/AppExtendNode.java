package org.dhorse.api.response.model;

public class AppExtendNode extends AppExtendNodejs {

	private static final long serialVersionUID = 1L;

	/**
	 * 文件路径
	 */
	private String packageTargetPath;

	public String getPackageTargetPath() {
		return packageTargetPath;
	}

	public void setPackageTargetPath(String packageTargetPath) {
		this.packageTargetPath = packageTargetPath;
	}

}