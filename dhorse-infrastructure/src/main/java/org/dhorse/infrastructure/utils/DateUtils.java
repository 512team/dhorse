package org.dhorse.infrastructure.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
	
	public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
	
	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	
	public static final String DATE_FORMAT_UTC_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	public static final String DATE_FORMAT_YYYYMMDD_HHMMSS = "yyyyMMdd_HHmmss";
	
	/**
	 * 使用默认格式化日期：yyyy-MM-dd HH:mm:ss
	 */
	public static String formatDefault(Date date) {
		return format(date, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
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
	
	public static String format(String utcDateStr) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_UTC_YYYY_MM_DD_HH_MM_SS);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return format(df.parse(utcDateStr), DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
		} catch (ParseException e) {
			logger.error("Failed to parse date", e);
		}
		return null;
	}
	
	public static Date parseDateUseUTC(String utcDateStr) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_UTC_YYYY_MM_DD_HH_MM_SS);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return df.parse(utcDateStr);
		} catch (ParseException e) {
			logger.error("Failed to parse date", e);
		}
		return null;
	}
	
	public static Date parse(String utcDateStr) {
        ZonedDateTime dateTime = ZonedDateTime.parse(utcDateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Date.from(dateTime.toInstant());
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
