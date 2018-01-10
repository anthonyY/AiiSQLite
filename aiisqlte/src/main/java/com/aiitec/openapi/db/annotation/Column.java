package com.aiitec.openapi.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段名注解
 * 原来使用column
 * 现在改为value 并把column设为过时
 * @version 1.0.1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    @Deprecated
    String column() default "";
    String value() default "";

}