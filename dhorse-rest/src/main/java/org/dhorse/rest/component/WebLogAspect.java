package org.dhorse.rest.component;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * 请求日志
 * 
 * @author 天地之怪
 */
@Aspect
@Component
public class WebLogAspect {

	private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

	private static final String[] IGNORE_PARAMS = new String[] { "serialVersionUID", "password",
			"authToken", "authName", "authPassword",
			"CASE_INSENSITIVE_ORDER", "hash", "response"};

	private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

	@Autowired
	private HttpServletRequest httpServletRequest;

	@Pointcut("execution(public * org.dhorse.rest.resource.*.*(..))")
	public void webLog() {
	}

	@Before("webLog()")
	public void doBefore(JoinPoint joinPoint) {
		startTime.set(System.currentTimeMillis());
		logger.info("request url: {}, ip: {}", httpServletRequest.getRequestURL().toString(),
				httpServletRequest.getRemoteAddr());
		Object[] args = joinPoint.getArgs();
		if (args == null || args.length == 0 || args[0] == null) {
			return;
		}
		for (Object arg : args) {
			logger.info("request params: {}", JsonUtils.toJsonString(arg, IGNORE_PARAMS));
		}
	}

	@AfterReturning(returning = "ret", pointcut = "webLog()")
	public void doAfterReturning(Object ret) {
		logger.info("request response time: {} ms", (System.currentTimeMillis() - startTime.get()));
		startTime.remove();
	}

}