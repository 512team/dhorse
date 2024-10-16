package org.dhorse.api.response.model;

/**
 * 环境自动配置配置
 * 
 * @author 无双
 */
public class EnvAutoDeployment extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 代码类型，见：{@link org.dhorse.api.enums.CodeTypeEnum}
	 */
	private Integer codeType;

	/**
	 * 分支（标签）名称
	 */
	private String branchName;

	/**
	 * cron表达式
	 */
	private String cron;

	/**
	 * 开启状态，见：{@link org.dhorse.api.enums.YesOrNoEnum}
	 */
	private Integer enable;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public Integer getCodeType() {
		return codeType;
	}

	public void setCodeType(Integer codeType) {
		this.codeType = codeType;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public Integer getEnable() {
		return enable;
	}

	public void setEnable(Integer enable) {
		this.enable = enable;
	}

}