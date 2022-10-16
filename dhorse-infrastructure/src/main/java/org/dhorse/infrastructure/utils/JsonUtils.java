package org.dhorse.infrastructure.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonUtils {

	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

	/**
	 * Json字符串反序列化为对象
	 *
	 * @param json  序列化字符串
	 * @param clazz 目标对象类型
	 * @param <T>
	 * @return
	 */
	public static <T> T parseToObject(String json, Class<T> clazz) {
		try {
			return getObjectMapper().readValue(json, clazz);
		} catch (IOException e) {
			logger.error("failed to parse clazz", e);
		}
		return null;
	}

	public static <T> T parseToObject(String json, JavaType type) {
		try {
			return getObjectMapper().readValue(json, type);
		} catch (IOException e) {
			logger.error("failed to parse type", e);
		}
		return null;
	}
	
	/**
	 * Json字符串反序列化为List
	 *
	 * @param json  序列化字符串
	 * @param clazz 目标对象类型
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> parseToList(String json, Class<T> clazz) {
		JavaType javaType = getObjectMapper().getTypeFactory().constructParametricType(List.class, clazz);
		return parseToObject(json, javaType);
	}

	public static JsonNode parseToNode(String json) {
		try {
			return getObjectMapper().readTree(json);
		} catch (IOException e) {
			logger.error("failed to parse node", e);
		}
		return null;
	}

	/**
	 * 将对象转换为json字符串
	 *
	 * @param object 对象
	 * @return json字符串
	 */
	public static String toJsonString(Object object, String... ignoreFields) {
		SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept(ignoreFields);
		FilterProvider filterProvider = new SimpleFilterProvider()
				.addFilter(IgnoreFieldsAnnotationIntrospector.FILTER_NAME, filter);
		ObjectMapper mapper = getObjectMapper();
		mapper.setAnnotationIntrospector(new IgnoreFieldsAnnotationIntrospector());
		try {
			return mapper.writer(filterProvider).writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("failed to serialize json", e);
		}
		return null;
	}

	public static ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SimpleDateFormat smt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		objectMapper.setDateFormat(smt);
		objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));// 解决时区差8小时问题
		return objectMapper;
	}

	private static class IgnoreFieldsAnnotationIntrospector extends JacksonAnnotationIntrospector {

		private static String FILTER_NAME = "fieldFilter";

		private static final long serialVersionUID = 1L;

		@Override
		public Object findFilterId(Annotated a) {
			return FILTER_NAME;
		}
	}
}
