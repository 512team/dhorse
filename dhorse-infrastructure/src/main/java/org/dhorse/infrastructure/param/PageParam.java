package org.dhorse.infrastructure.param;

public abstract class PageParam extends BaseParam {

	private static final long serialVersionUID = 1L;

	protected Integer pageSize = 10;

	protected Integer pageNum;

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