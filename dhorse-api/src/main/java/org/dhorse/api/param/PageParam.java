package org.dhorse.api.param;

import java.io.Serializable;

public abstract class PageParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 每页条数
	 */
	private Integer pageSize = 10;

	/**
	 * 页码
	 */
	private Integer pageNum;

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

}
