package org.dhorse.infrastructure.param;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 日志记录
 * 
 * @author 无双
 */
@TableName("LOG_RECORD")
public class LogRecordParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 业务编号
	 */
	private String bizId;

	/**
	 * 日志类型，1：构建版本日志，2：部署日志
	 */
	private Integer logType;

	/**
	 * 日志内容
	 */
	private String content;

	public String getBizId() {
		return bizId;
	}

	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

	public Integer getLogType() {
		return logType;
	}

	public void setLogType(Integer logType) {
		this.logType = logType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}