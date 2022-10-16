package org.dhorse.api.vo;

import java.io.Serializable;
import java.util.Date;

public abstract class BaseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	private String id;

	/**
	 * 修改权限，0：无权限，1：有权限
	 */
	private Integer modifyRights = 0;

	/**
	 * 删除权限，0：无权限，1：有权限
	 */
	private Integer deleteRights = 0;

	/**
	 * 创建时间
	 */
	private Date creationTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	public String getId() {
		if (id == null) {
			return null;
		}
		return id.toString();
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getModifyRights() {
		return modifyRights;
	}

	public void setModifyRights(Integer modifyRights) {
		this.modifyRights = modifyRights;
	}

	public Integer getDeleteRights() {
		return deleteRights;
	}

	public void setDeleteRights(Integer deleteRights) {
		this.deleteRights = deleteRights;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
