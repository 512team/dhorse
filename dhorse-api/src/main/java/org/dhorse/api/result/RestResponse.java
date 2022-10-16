package org.dhorse.api.result;

import java.io.Serializable;

import org.dhorse.api.enums.MessageCodeEnum;

public class RestResponse<D> implements Result<D>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 响应码
	 */
	private String code = MessageCodeEnum.SUCESS.getCode();

	/**
	 * 响应信息
	 */
	private String message = MessageCodeEnum.SUCESS.getMessage();

	/**
	 * 业务数据
	 */
	private D data;

	public RestResponse() {

	}

	public RestResponse(D data) {
		this.data = data;
	}
	
	public RestResponse(MessageCodeEnum messageCode) {
		this.code = messageCode.getCode();
		this.message = messageCode.getMessage();
	}
	
	public RestResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public D getData() {
		return data;
	}

	public void setData(D data) {
		this.data = data;
	}
}
