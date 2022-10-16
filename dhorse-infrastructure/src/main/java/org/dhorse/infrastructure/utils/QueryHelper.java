package org.dhorse.infrastructure.utils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.esotericsoftware.reflectasm.MethodAccess;

public class QueryHelper {

	private static final Logger logger = LoggerFactory.getLogger(QueryHelper.class);
	
	public static <T> QueryWrapper<T> buildQueryWrapper(T model, String orderField) {
		QueryWrapper<T> wrapper = new QueryWrapper<>();
		MethodAccess methodAccess = BeanUtils.getMethodAccess(model.getClass());
		Set<String> fields = BeanUtils.getFields(model.getClass());
		Class<?>[] returnTypes = methodAccess.getReturnTypes();
		try {
			for (String field : fields) {
				int index = BeanUtils.getMethodIndex(model.getClass().getName() + "#get" + field).intValue();
				Object value = methodAccess.invoke(model, index);
				if (value == null || "".equals(value)) {
					continue;
				}
				if(returnTypes[index].getName().equals(List.class.getName())) {
					wrapper.in(getColumnNameOfList(field), ((List<?>)value).toArray());
				}else {
					wrapper.eq(getColumnName(field), value);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to build query wrapper", e);
		}
		wrapper.eq("deletion_status", 0);
		if(!StringUtils.isBlank(orderField)) {
			wrapper.orderByDesc(orderField);
		}
		return wrapper;
	}

	public static <T> QueryWrapper<T> buildLikeRightWrapper(T model) {
		QueryWrapper<T> wrapper = new QueryWrapper<>();
		MethodAccess methodAccess = BeanUtils.getMethodAccess(model.getClass());
		Set<String> fields = BeanUtils.getFields(model.getClass());
		try {
			for (String field : fields) {
				int index = BeanUtils.getMethodIndex(model.getClass().getName() + "#get" + field).intValue();
				Object value = methodAccess.invoke(model, index);
				if (value == null || "".equals(value)) {
					continue;
				}
				wrapper.likeRight(getColumnName(field), value);
			}
		} catch (Exception e) {
			logger.error("Failed to build query wrapper", e);
		}
		wrapper.eq("deletion_status", 0);
		wrapper.orderByDesc("update_time");
		return wrapper;
	}
	
	public static <T> UpdateWrapper<T> buildUpdateWrapper(T model) {
		UpdateWrapper<T> wrapper = new UpdateWrapper<>();
		MethodAccess methodAccess = BeanUtils.getMethodAccess(model.getClass());
		Set<String> fields = BeanUtils.getFields(model.getClass());
		Class<?>[] returnTypes = methodAccess.getReturnTypes();
		try {
			for (String field : fields) {
				int index = BeanUtils.getMethodIndex(model.getClass().getName() + "#get" + field).intValue();
				Object value = methodAccess.invoke(model, index);
				if (value == null || "".equals(value)) {
					continue;
				}
				if(returnTypes[index].getName().equals(List.class.getName())) {
					wrapper.in(getColumnNameOfList(field), ((List<?>)value).toArray());
				}else {
					wrapper.eq(getColumnName(field), value);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to build update wrapper", e);
		}
		return wrapper;
	}
	
	private static String getColumnName(String name) {
		return cutColumnName(name, name.length());
	}
	
	private static String getColumnNameOfList(String name) {
		return cutColumnName(name, name.length() - 1);
	}
	
	private static String cutColumnName(String name, int lastIndexOfChar) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lastIndexOfChar; i++) {
			char chat = name.charAt(i);
			if(i == 0) {
				sb.append(String.valueOf(chat).toLowerCase(Locale.ROOT));
			}else if (chat > 64 && chat < 91) {
				sb.append("_");
				sb.append(String.valueOf(chat).toLowerCase(Locale.ROOT));
			} else {
				sb.append(chat);
			}
		}
		return sb.toString();
	}
}