package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 环境扩展
 * 
 * @author Dahai 2023-03-22
 */
@TableName("ENV_EXT")
public class EnvExtPO extends BaseAppPO {

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
	 * 扩展类型，1：技术类型，2：亲和容忍，3：健康检查
	 */
	private int exType;

	/**
	 * 扩展内容
	 */
	private String ext;

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

	public int getExType() {
		return exType;
	}

	public void setExType(int exType) {
		this.exType = exType;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	@Override
	public List<String> getIds() {
		return this.ids;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Integer getDeletionStatus() {
		return this.deletionStatus;
	}

}