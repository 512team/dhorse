package org.dhorse.infrastructure.utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.dhorse.infrastructure.strategy.login.dto.LoginUser;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCacheUtils {

	private static long MAX_SIZE = 65536L;

	private static int INIT_CAPACITY = 1024;

	private static TimeUnit DEAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

	private static Cache<String, LoginUser> LOGINED_USER_CACHE = CacheBuilder.newBuilder()
			.maximumSize(MAX_SIZE)
			.initialCapacity(INIT_CAPACITY)
			.recordStats()
			.expireAfterWrite(Constants.LOGINED_VALID_MILLISECONDS_TIME, DEAULT_TIME_UNIT)
			.build();
	
	private static Cache<String, String> LOGIN_NAME_TOKEN_CACHE = CacheBuilder.newBuilder()
			.maximumSize(MAX_SIZE)
			.initialCapacity(INIT_CAPACITY)
			.recordStats()
			.expireAfterWrite(Constants.LOGINED_VALID_MILLISECONDS_TIME, DEAULT_TIME_UNIT)
			.build();

	public static void putLoginUser(String loginToken, LoginUser loginUser) {
		LOGINED_USER_CACHE.put(loginToken, loginUser);
		LOGIN_NAME_TOKEN_CACHE.put(loginUser.getLoginName(), loginToken);
	}

	public static LoginUser getLoginUserByToken(String loginToken) {
		return LOGINED_USER_CACHE.getIfPresent(loginToken);
	}
	
	public static LoginUser getLoginUserByLoginName(String loginName) {
		String loginToken = LOGIN_NAME_TOKEN_CACHE.getIfPresent(loginName);
		if(Objects.isNull(loginToken)) {
			return null;
		}
		return LOGINED_USER_CACHE.getIfPresent(loginToken);
	}
	
	public static void removeLoginUserByToken(String loginToken) {
		LoginUser loginUser = LOGINED_USER_CACHE.getIfPresent(loginToken);
		if(Objects.isNull(loginUser)) {
			return;
		}
		LOGIN_NAME_TOKEN_CACHE.invalidate(loginUser.getLoginName());
		LOGINED_USER_CACHE.invalidate(loginToken);
	}
	
	public static void removeLoginUserByLoginName(String loginName) {
		String loginToken = LOGIN_NAME_TOKEN_CACHE.getIfPresent(loginName);
		if(Objects.isNull(loginToken)) {
			return;
		}
		LOGIN_NAME_TOKEN_CACHE.invalidate(loginName);
		LOGINED_USER_CACHE.invalidate(loginToken);
	}
}
