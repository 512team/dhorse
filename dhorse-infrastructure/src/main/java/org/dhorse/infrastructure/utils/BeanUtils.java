package org.dhorse.infrastructure.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.reflectasm.MethodAccess;

public class BeanUtils {

	private static Map<Class<?>, MethodAccess> methodMap = new HashMap<>();

	private static Map<String, Integer> methodIndexMap = new HashMap<>();

	private static Map<Class<?>, Set<String>> fieldMap = new HashMap<>();

	public static void copyProperties(Object source, Object target) {
		MethodAccess sourceMethodAccess = getMethodAccess(source.getClass());
		MethodAccess targetMethodAccess = getMethodAccess(target.getClass());
		Set<String> fields = fieldMap.get(source.getClass());
		for (String field : fields) {
			String getKey = source.getClass().getName() + "#get" + field;
			String setkey = target.getClass().getName() + "#set" + field;
			Integer setIndex = methodIndexMap.get(setkey);
			if (setIndex != null) {
				int getIndex = methodIndexMap.get(getKey);
				targetMethodAccess.invoke(target, setIndex.intValue(), sourceMethodAccess.invoke(source, getIndex));
			}
		}
	}

	public static Set<String> getFields(Class<?> clazz) {
		return fieldMap.get(clazz);
	}
	
	public static Integer getMethodIndex(String method) {
		return methodIndexMap.get(method);
	}
	
	public static MethodAccess getMethodAccess(Class<?> clazz) {
		MethodAccess targetMethodAccess = methodMap.get(clazz);
		if (targetMethodAccess != null) {
			return targetMethodAccess;
		}
		synchronized (clazz) {
			targetMethodAccess = methodMap.get(clazz);
			if (targetMethodAccess != null) {
				return targetMethodAccess;
			}
			MethodAccess methodAccess = MethodAccess.get(clazz);
			String[] methods = methodAccess.getMethodNames();
			Set<String> fields = new HashSet<String>(methods.length);
			for (String method : methods) {
				methodIndexMap.put(clazz.getName() + "#" + method, methodAccess.getIndex(method));
				fields.add(method.substring(3));
			}
			fieldMap.put(clazz, fields);
			methodMap.put(clazz, methodAccess);
			return methodAccess;
		}
	}

}
