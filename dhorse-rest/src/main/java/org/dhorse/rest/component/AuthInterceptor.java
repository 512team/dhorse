package org.dhorse.rest.component;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dhorse.infrastructure.utils.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.response.model.SysUser;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.dhorse.infrastructure.annotation.AccessOnlyAdmin;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 
 * 权限拦截
 * 
 * @author Dahai
 */
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private SysUserApplicationService sysUserApplicationService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if (!(handler instanceof HandlerMethod)) {
			return true;
		}

		// 不登录允许访问
		Method method = ((HandlerMethod) handler).getMethod();
		if (hasAnnotation(method, AccessNotLogin.class)) {
			return true;
		}

		// 登录允许访问
		SysUser loginUser = getLoginUser(request);
		if (loginUser == null) {
			if (hasAnnotation(method, RestController.class)) {
				restLogin(response);
			} else {
				webLogin(request, response);
			}
			return false;
		}

		// admin访问
		if ((hasAnnotation(method, AccessOnlyAdmin.class))
				&& !RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			if (hasAnnotation(method, RestController.class)) {
				restRight(response);
			} else {
				webRight(response);
			}
			return false;
		}

		return true;
	}

	private boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
		return method.getDeclaringClass().isAnnotationPresent(annotationClass)
				|| method.isAnnotationPresent(annotationClass);
	}

	private String getCookieValue(HttpServletRequest request, String key) {
		if (key == null) {
			return null;
		}
		
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return request.getHeader(key);
		}

		for (Cookie cookie : cookies) {
			if (key.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		return null;
	}

	private LoginUser getLoginUser(HttpServletRequest request) {
		String loginToken = getCookieValue(request, "login_token");
		if (StringUtils.isBlank(loginToken)) {
			return null;
		}

		return sysUserApplicationService.queryLoginUserByToken(loginToken);
	}

	private void restLogin(HttpServletResponse response) throws IOException {
		restResponse(response, MessageCodeEnum.SYS_USER_NOT_LOGINED);
	}

	private void restRight(HttpServletResponse response) throws IOException {
		restResponse(response, MessageCodeEnum.NO_ACCESS_RIGHT);
	}

	private void restResponse(HttpServletResponse response, MessageCodeEnum messageCodeEnum) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		ServletOutputStream out = response.getOutputStream();
		out.write(JsonUtils.toJsonString(new RestResponse<>(messageCodeEnum)).getBytes());
	}

	private void webRight(HttpServletResponse response) throws IOException {
		response.sendRedirect("/user/noRight.html");
	}

	private void webLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("login.html");
	}

}