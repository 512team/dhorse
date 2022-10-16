package org.dhorse.infrastructure.utils;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * 默认日志路径。
 */
public class DefaultLogPath extends PropertyDefinerBase {

	@Override
	public String getPropertyValue() {
		return Constants.DEFAULT_LOG_PATH;
	}
}