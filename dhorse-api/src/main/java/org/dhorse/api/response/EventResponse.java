package org.dhorse.api.response;

import java.io.Serializable;

/**
 * 事件数据模型
 * 
 * @author Dahai 2022-03-12
 */
public class EventResponse implements Response, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 事件码，见org.dhorse.api.enums.EventCodeEnum
	 */
	private String eventCode;

	/**
	 * 环境标识
	 */
	private String envTag;

	/**
	 * 事件数据，json格式
	 */
	private String data;

	public EventResponse() {

	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getEnvTag() {
		return envTag;
	}

	public void setEnvTag(String envTag) {
		this.envTag = envTag;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}