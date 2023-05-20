package org.dhorse.infrastructure.utils;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.slf4j.Logger;

public class LogUtils {

	public static void throwException(Logger logger, MessageCodeEnum messageCode){
		logger.warn("code: {}, message: {}", messageCode.getCode(), messageCode.getMessage());
		throw new ApplicationException(messageCode.getCode(), messageCode.getMessage());
	}
	
	public static void throwException(Logger logger, Throwable e, MessageCodeEnum messageCode){
		logger.error("code: "+ messageCode.getCode() +", message: " + messageCode.getMessage(), e);
		throw new ApplicationException(messageCode.getCode(), messageCode.getMessage());
	}
	
	public static void throwException(Logger logger, String detail, MessageCodeEnum messageCode){
		logger.error("code: {}, message: {}, detail: {}", messageCode.getCode(), messageCode.getMessage(), detail);
		throw new ApplicationException(messageCode.getCode(), messageCode.getMessage());
	}
}
