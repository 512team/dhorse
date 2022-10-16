package org.dhorse.infrastructure.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtils {

	private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);
	
	/**
	 * 
	* 泛型的第一个参数的实例。
	*
	* @param type
	* @return
	 */
	public static <T> T newFirstParameterizedInstance(Type type) {
		return newInstance(getClazz(type, 0));
	}
	
	/**
	 * 
	* 泛型参数的实例
	*
	* @param type
	* @param genericArgumentIndex
	* @return
	 */
	public static <T> T newParameterizedTypeInstance(Type type, int genericArgumentIndex) {
		return newInstance(getClazz(type, genericArgumentIndex));
	}
	
	/**
	 * 
	* 获取指定泛型参数的类型。
	*
	* @param type
	* @param genericArgumentIndex
	* @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClazz(Type type, int genericArgumentIndex) {
		return (Class<T>) (((ParameterizedType) type).getActualTypeArguments()[genericArgumentIndex]);
	}

	public static <T> T newInstance(Class<T> clazz) {
		T o = null;
		try {
			o = clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("failed to newInstance, class:" + clazz, e);
		}
		return o;
	}
}
