package org.dhorse.api.response;

import java.io.Serializable;
import java.util.List;

public class PageData<D> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 当前页
	 */
	private Integer pageNum;

	/**
	 * 总页数
	 */
	private Integer pageCount;

	/**
	 * 页大小
	 */
	private Integer pageSize;

	/**
	 * 总记录数
	 */
	private Integer itemCount;

	/**
	 * 结果集
	 */
	private List<D> items;

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getItemCount() {
		return itemCount;
	}

	public void setItemCount(Integer itemCount) {
		this.itemCount = itemCount;
	}

	public List<D> getItems() {
		return items;
	}

	public void setItems(List<D> items) {
		this.items = items;
	}

}
