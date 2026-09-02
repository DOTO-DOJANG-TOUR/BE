package com.doto.global.error;

import com.doto.global.api.CommonResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** 애플리케이션과 Spring MVC 예외를 {@link CommonResponse} 형식으로 변환하는 전역 처리기 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 도메인 예외를 정의된 상태 코드로 변환
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<CommonResponse<Void>> handleDomainException(DomainException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("[DOMAIN_ERROR] code={}, status={}", errorCode.getCode(), errorCode.getStatus(), exception);
        } else {
            log.warn("[DOMAIN_ERROR] code={}, status={}, message={}",
                    errorCode.getCode(), errorCode.getStatus(), exception.getMessage());
        }
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    // 요청 DTO 외부의 Bean Validation 오류를 필드별로 변환
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage());
        }

        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode, fieldErrors));
    }

    // DB 제약 조건 위반은 클라이언트 요청 간 상태 충돌로 변환
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        ErrorCode errorCode = CommonErrorCode.CONFLICT;
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<CommonResponse<Void>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception
    ) {
        ErrorCode errorCode = CommonErrorCode.CONFLICT;
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    // 처리되지 않은 예외는 내부 정보를 숨기고 500으로 변환
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("[ERROR] 서버 내부 오류: ", exception);
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    // @Valid 요청 본문의 필드 검증 오류를 변환
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode, fieldErrors),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 요청 파라미터의 메서드 검증 오류를 변환
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 읽을 수 없는 JSON 요청 본문을 변환
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.MALFORMED_REQUEST;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 누락된 필수 요청 파라미터를 변환
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 요청 값의 타입 변환 실패를 변환
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 요청 데이터 바인딩 실패를 변환
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 지원하지 않는 HTTP 메서드 요청을 변환
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 지원하지 않는 Content-Type 요청을 변환
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.UNSUPPORTED_MEDIA_TYPE;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    // 응답 직렬화 실패를 기록하고 500으로 변환
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.error("Response serialization failed", exception);

        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return handleExceptionInternal(
                exception,
                CommonResponse.error(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }
}
