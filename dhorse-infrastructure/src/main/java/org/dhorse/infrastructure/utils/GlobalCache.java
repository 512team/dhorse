package org.dhorse.infrastructure.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局缓存
 *
 * @author Dahai
 **/
public class GlobalCache {
	
	public static final Map<String, Long> CODEUP_REPOSITORY_ID = new ConcurrentHashMap<>();

}
