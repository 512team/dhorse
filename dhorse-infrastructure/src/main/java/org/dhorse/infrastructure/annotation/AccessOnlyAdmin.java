package org.dhorse.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
* 只有admin角色才能访问
* 
* @author Dahai
* @Date 20121年12月5日 下午9:05:53
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessOnlyAdmin {

}