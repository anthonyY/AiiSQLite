package com.aiitec.openapi.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Anthony
 * 表名注解
 * 原来使用name字段
 * 现在改为value 并把name 设置为过时
 *
 * @version 1.0.1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    @Deprecated
    String name() default "";

    String value() default "";
}
