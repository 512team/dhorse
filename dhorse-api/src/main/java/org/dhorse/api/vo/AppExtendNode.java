package org.dhorse.api.vo;

import org.dhorse.api.vo.App.AppExtend;

public class AppExtendNode extends AppExtend {

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