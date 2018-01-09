package com.aiitec.openapi.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author  Anthony
 * 时间格式化的注解
 * @version 1.0.1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
    String value() default "yyyy-MM-dd HH:mm:ss";
}