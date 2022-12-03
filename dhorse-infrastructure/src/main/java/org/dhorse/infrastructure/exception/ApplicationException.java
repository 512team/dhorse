package org.dhorse.infrastructure.exception;

import org.dhorse.api.enums.MessageCodeEnum;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String code;

	private String message;

	public ApplicationException(MessageCodeEnum messageCode) {
		this.code = messageCode.getCode();
		this.message = messageCode.getMessage();
	}

	public ApplicationException(String code, String message) {
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

}