package org.dhorse.infrastructure.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
	
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
	
	public static String formatLocal(String utcDateStr) {
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_UTC_YYYY_MM_DD_HH_MM_SS);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return format(df.parse(utcDateStr), PATTERN_DEFAULT);
		} catch (ParseException e) {
			logger.error("Failed to parse date", e);
		}
		return null;
	}
	
	public static Date formatLocal2(String utcDateStr) {
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_UTC_YYYY_MM_DD_HH_MM_SS);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return df.parse(utcDateStr);
		} catch (ParseException e) {
			logger.error("Failed to parse date", e);
		}
		return null;
	}
	
	public static Date addDays(final Date date, final int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }
	
	private static Date add(final Date date, final int calendarField, final int amount) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }
}
