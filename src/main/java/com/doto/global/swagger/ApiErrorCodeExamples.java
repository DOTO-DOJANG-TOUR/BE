package com.doto.global.swagger;

import com.doto.global.error.ErrorCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExamples {

    Class<? extends ErrorCode>[] value();
}
