package org.dhorse.infrastructure.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	
	public static final String PATTERN_HH_MM_SS = "HH:mm:ss";

	/**
	 * 使用默认格式化日期：yyyy-MM-dd HH:mm:ss
	 */
	public static String formatDefault(Date date) {
		return format(date, PATTERN_DEFAULT);
	}

	/**
	 * 格式化日期
	 */
	public static String format(Date date, String format) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat(format).format(date);
	}
}
