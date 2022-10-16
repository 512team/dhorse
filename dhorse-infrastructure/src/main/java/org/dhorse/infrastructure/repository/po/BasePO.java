package org.dhorse.infrastructure.repository.po;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;

public abstract class BasePO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	protected String id;
	
	/**
	 * 创建时间
	 */
	private Date creationTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	protected Integer deletionStatus;

	@TableField(exist = false)
	protected List<String> ids;

	public abstract String getId();

	public void setId(String id) {
		this.id = id;
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

	public abstract Integer getDeletionStatus();

	public void setDeletionStatus(Integer deletionStatus) {
		this.deletionStatus = deletionStatus;
	}

	public abstract List<String> getIds();

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

}
