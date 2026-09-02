package com.doto.global.api;

import com.doto.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** API 성공과 실패 응답을 동일한 형식으로 제공하는 공통 응답 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T>(
        @JsonProperty("isSuccess") boolean success,
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        T result
) {

    public static <T> CommonResponse<T> success(T result) {
        return success(CommonSuccessCode.OK, result);
    }

    public static <T> CommonResponse<T> success(SuccessCode successCode, T result) {
        return new CommonResponse<>(true, successCode.getCode(), successCode.getMessage(), result);
    }

    public static CommonResponse<Void> error(ErrorCode errorCode) {
        return new CommonResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> CommonResponse<T> error(ErrorCode errorCode, T result) {
        return new CommonResponse<>(false, errorCode.getCode(), errorCode.getMessage(), result);
    }
}
