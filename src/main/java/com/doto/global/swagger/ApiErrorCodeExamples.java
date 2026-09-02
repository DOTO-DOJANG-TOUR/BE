package com.doto.global.swagger;

import com.doto.global.error.ErrorCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 도메인 Api 인터페이스(TYPE)에 붙이면 모든 operation에 해당 ErrorCode들이 공통으로 등록된다.
 * 특정 operation만 다른 ErrorCode 조합이 필요하면 그 메서드(METHOD)에 직접 붙인다.
 * 메서드에 선언하면 타입 레벨 선언은 그 메서드에 한해 무시되고 메서드 선언만 적용된다.
 * 아무 도메인 에러도 없는 operation은 {@code @ApiErrorCodeExamples({})}로 빈 값을 선언한다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExamples {

    Class<? extends ErrorCode>[] value();
}
