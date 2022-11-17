package org.dhorse.api.param.app.env.replica;

/**
 * 下载文件参数模型
 * 
 * @author Dahai
 */
public class DownloadFileParam extends EnvReplicaParam {

	private static final long serialVersionUID = 1L;

	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}