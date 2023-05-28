package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 部署版本
 * 
 * @author Dahai 2021-09-08
 */
@TableName("DEPLOYMENT_VERSION")
public class DeploymentVersionPO extends BaseAppPO {

	private static final long serialVersionUID = 1L;

	/**
	 * 分支编号
	 */
	private String branchName;

	/**
	 * 镜像名称（包含tag）
	 */
	private String versionName;

	/**
	 * 环境编号
	 */
	private String envId;

	/**
	 * 环境编号
	 */
	@TableField(exist = false)
	private List<String> envIds;

	/**
	 * 状态，0：构建中，1：构建成功，2：构建失败
	 */
	private Integer status;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public List<String> getEnvIds() {
		return envIds;
	}

	public void setEnvIds(List<String> envIds) {
		this.envIds = envIds;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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