package org.dhorse.api.param.app.env;

/**
 * 修改应用环境
 * 
 * @author Dahai
 */
public class AppEnvUpdateParam extends AppEnvCreationParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用环境编号
	 */
	private String appEnvId;

	public String getAppEnvId() {
		return appEnvId;
	}

	public void setAppEnvId(String appEnvId) {
		this.appEnvId = appEnvId;
	}

}